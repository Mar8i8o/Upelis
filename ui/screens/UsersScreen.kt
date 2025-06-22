package com.example.upelis_mariomarin.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                authViewModel.loadUsername()
                authViewModel.loadUserProfilePhoto()
                authViewModel.loadFriends()
                authViewModel.loadAllUsers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val user = authViewModel.currentUser
    val profilePhotoUrl by authViewModel.profilePhotoUrl.collectAsState()
    val username by authViewModel.username.collectAsState()
    val friendsList by authViewModel.friendsList.collectAsState()
    val allUsersList by authViewModel.allUsersList.collectAsState()

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            authViewModel.updateProfilePhoto(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets(0))
                    .offset(y = (-50).dp)
                    .padding(vertical = 10.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perfil", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de perfil
            if (!profilePhotoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Subir foto",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre de usuario
            if (username.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nombre: $username", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Email
            user?.email?.let { email ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(email, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text("Amigos", fontSize = 18.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                friendsList.forEach { friend ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(80.dp)
                    ) {
                        if (!friend.profilePhotoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = friend.profilePhotoUrl,
                                contentDescription = "Amigo",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(friend.username.ifEmpty { "Amigo" }, fontSize = 12.sp, maxLines = 1)

                        IconButton(
                            onClick = {
                                authViewModel.removeFriend(friend.uid)
                                authViewModel.loadFriends()
                                authViewModel.loadAllUsers()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    authViewModel.loadAllUsers()
                    showAddFriendDialog = true
                    searchQuery = ""
                }
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar amigo", color = Color.White)
            }
        }
    }

    // Diálogo para agregar amigo (sin cambios)
    if (showAddFriendDialog) {
        val currentUserId = authViewModel.currentUser?.uid
        val filteredUsers = allUsersList.filter { user ->
            user.username.isNotBlank() &&
                    user.uid != currentUserId &&
                    friendsList.none { friend -> friend.uid == user.uid } &&
                    (searchQuery.isBlank() || user.username.contains(searchQuery, ignoreCase = true))
        }

        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Agregar amigo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,   // Borde cuando está seleccionado
                            unfocusedBorderColor = Color.Gray,         // Borde cuando no está seleccionado
                            focusedLabelColor = Color.White,     // Color del label al enfocar
                            cursorColor = Color.White            // Color del cursor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredUsers.isEmpty()) {
                        Text("No se encontraron usuarios", modifier = Modifier.padding(16.dp))
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            filteredUsers.forEach { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            authViewModel.addFriend(user.uid)
                                            showAddFriendDialog = false
                                            authViewModel.loadFriends()
                                            authViewModel.loadAllUsers()
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!user.profilePhotoUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = user.profilePhotoUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(user.username, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

