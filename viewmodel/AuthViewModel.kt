package com.example.upelis_mariomarin.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _isUserAuthenticated = MutableStateFlow(firebaseAuth.currentUser != null)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _profilePhotoUrl = MutableStateFlow<String?>(null)
    val profilePhotoUrl: StateFlow<String?> = _profilePhotoUrl

    val currentUser = firebaseAuth.currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        _isUserAuthenticated.value = true
                        loadUsername()
                        loadUserProfilePhoto()
                    } else {
                        _errorMessage.value = task.exception?.localizedMessage ?: "Error desconocido"
                        _isUserAuthenticated.value = false
                    }
                }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid
                        if (userId != null) {
                            val userMap = mapOf(
                                "username" to username,
                                "email" to email,
                                "profilePhotoUrl" to "" // <-- campo inicial vacío o default
                            )
                            database.child("users").child(userId).setValue(userMap)
                                .addOnCompleteListener { dbTask ->
                                    _isLoading.value = false
                                    if (dbTask.isSuccessful) {
                                        _isUserAuthenticated.value = true
                                        _username.value = username
                                        _profilePhotoUrl.value = ""
                                    } else {
                                        _errorMessage.value = dbTask.exception?.localizedMessage ?: "Error al guardar el usuario"
                                        _isUserAuthenticated.value = false
                                    }
                                }
                        } else {
                            _isLoading.value = false
                            _errorMessage.value = "No se pudo obtener el ID de usuario"
                        }
                    } else {
                        _isLoading.value = false
                        _errorMessage.value = task.exception?.localizedMessage ?: "Error desconocido"
                        _isUserAuthenticated.value = false
                    }
                }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _isUserAuthenticated.value = false
        _username.value = ""
        _profilePhotoUrl.value = null
    }

    fun loadUsername() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("username")
            .get()
            .addOnSuccessListener { snapshot ->
                _username.value = snapshot.getValue(String::class.java) ?: ""
            }
            .addOnFailureListener {
                _username.value = ""
            }
    }

    fun loadUserProfilePhoto() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("profilePhotoUrl")
            .get()
            .addOnSuccessListener { snapshot ->
                _profilePhotoUrl.value = snapshot.getValue(String::class.java)
            }
            .addOnFailureListener {
                _profilePhotoUrl.value = null
            }
    }

    // Nueva función para actualizar la foto de perfil
    fun updateProfilePhoto(uri: Uri) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        _isLoading.value = true
        _errorMessage.value = ""

        // Ruta en Storage: photos/{userId}/profile.jpg
        val photoRef = storage.child("photos/$userId/profile.jpg")

        photoRef.putFile(uri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val photoUrl = downloadUri.toString()
                    // Guardamos URL en Realtime Database
                    database.child("users").child(userId).child("profilePhotoUrl")
                        .setValue(photoUrl)
                        .addOnSuccessListener {
                            _profilePhotoUrl.value = photoUrl
                            _isLoading.value = false
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = e.localizedMessage ?: "Error al guardar URL de foto"
                            _isLoading.value = false
                        }
                }.addOnFailureListener { e ->
                    _errorMessage.value = e.localizedMessage ?: "Error al obtener URL de descarga"
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.localizedMessage ?: "Error al subir la foto"
                _isLoading.value = false
            }
    }
}
