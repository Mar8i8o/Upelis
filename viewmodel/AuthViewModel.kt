package com.example.upelis_mariomarin.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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

    val currentUser get() = firebaseAuth.currentUser

    private val _friendsList = MutableStateFlow<List<User>>(emptyList())
    val friendsList: StateFlow<List<User>> = _friendsList

    private val _allUsersList = MutableStateFlow<List<User>>(emptyList())
    val allUsersList: StateFlow<List<User>> = _allUsersList

    private var usersListener: ValueEventListener? = null

    data class User(
        val uid: String = "",
        val username: String = "",
        val email: String = "",
        val profilePhotoUrl: String = ""
    )

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
                        loadFriends()
                        startObservingAllUsers()
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
                        val userId = currentUser?.uid
                        if (userId != null) {
                            val userMap = mapOf(
                                "username" to username,
                                "email" to email,
                                "profilePhotoUrl" to ""
                            )
                            database.child("users").child(userId).setValue(userMap)
                                .addOnCompleteListener { dbTask ->
                                    _isLoading.value = false
                                    if (dbTask.isSuccessful) {
                                        _isUserAuthenticated.value = true
                                        _username.value = username
                                        _profilePhotoUrl.value = ""
                                        loadFriends()
                                        startObservingAllUsers()
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
        _friendsList.value = emptyList()
        _allUsersList.value = emptyList()
        stopObservingAllUsers()
    }

    fun loadUsername() {
        val userId = currentUser?.uid ?: return
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
        val userId = currentUser?.uid ?: return
        database.child("users").child(userId).child("profilePhotoUrl")
            .get()
            .addOnSuccessListener { snapshot ->
                _profilePhotoUrl.value = snapshot.getValue(String::class.java)
            }
            .addOnFailureListener {
                _profilePhotoUrl.value = null
            }
    }

    fun updateProfilePhoto(uri: Uri) {
        val userId = currentUser?.uid ?: return
        _isLoading.value = true
        _errorMessage.value = ""
        val photoRef = storage.child("photos/$userId/profile.jpg")
        photoRef.putFile(uri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val photoUrl = downloadUri.toString()
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
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.localizedMessage ?: "Error al subir la foto"
                _isLoading.value = false
            }
    }

    fun loadFriends() {
        val userId = currentUser?.uid ?: return
        database.child("friends").child(userId).get().addOnSuccessListener { snapshot ->
            val friendIds = snapshot.children.mapNotNull { it.key }
            if (friendIds.isEmpty()) {
                _friendsList.value = emptyList()
                return@addOnSuccessListener
            }

            val loadedFriends = mutableListOf<User>()
            var loadedCount = 0

            for (fid in friendIds) {
                database.child("users").child(fid).get().addOnSuccessListener { userSnap ->
                    val user = userSnap.getValue(User::class.java)?.copy(uid = fid)
                    user?.let { loadedFriends.add(it) }

                    loadedCount++
                    if (loadedCount == friendIds.size) {
                        _friendsList.value = loadedFriends
                    }
                }
            }
        }.addOnFailureListener {
            _errorMessage.value = "Error al cargar amigos"
        }
    }

    fun addFriend(friendUid: String) {
        val userId = currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "/friends/$userId/$friendUid" to true,
            "/friends/$friendUid/$userId" to true
        )

        database.updateChildren(updates).addOnSuccessListener {
            loadFriends()
        }.addOnFailureListener {
            _errorMessage.value = "Error al agregar amigo"
        }
    }

    fun removeFriend(friendUid: String) {
        val userId = currentUser?.uid ?: return

        val updates = hashMapOf<String, Any?>(
            "/friends/$userId/$friendUid" to null,
            "/friends/$friendUid/$userId" to null
        )

        database.updateChildren(updates).addOnSuccessListener {
            loadFriends()
        }.addOnFailureListener {
            _errorMessage.value = it.localizedMessage ?: "Error al eliminar amigo"
        }
    }

    // Listener para actualizar _allUsersList en tiempo real
    private fun startObservingAllUsers() {
        if (usersListener != null) return  // Ya está activo

        usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull {
                    val uid = it.key ?: return@mapNotNull null
                    val user = it.getValue(User::class.java)?.copy(uid = uid)
                    user
                }
                _allUsersList.value = users
            }

            override fun onCancelled(error: DatabaseError) {
                _errorMessage.value = "Error al cargar usuarios: ${error.message}"
            }
        }
        database.child("users").addValueEventListener(usersListener as ValueEventListener)
    }

    private fun stopObservingAllUsers() {
        usersListener?.let {
            database.child("users").removeEventListener(it)
            usersListener = null
        }
    }

    fun loadAllUsers() {
        startObservingAllUsers()
    }
}
