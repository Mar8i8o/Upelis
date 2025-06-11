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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.upelis_mariomarin.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
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
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
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
                        .clickable { launcher.launch("image/*") },
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
                    Text("Subir foto", color = Color.Gray, fontSize = 16.sp)
                }
            }

            if (username.isNotEmpty()) {
                Text(text = "Nombre de usuario: $username", fontSize = 18.sp)
            }

            if (user != null) {
                Text(
                    text = "Email: ${user.email}",
                    fontSize = 20.sp,
                )
            }

            Button(onClick = {
                authViewModel.logout()
                onBack()
            }) {
                Text("Cerrar sesión")
            }

            Spacer(modifier = Modifier.height(24.dp))

            /*
            // Mostrar todos los usuarios (incluido el propio) para test
            Text("Todos los usuarios (incluido tú):", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 150.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                allUsersList.forEach { user ->
                    Text(text = "- ${user.username} (${user.email ?: "sin email"})", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            */

            // Lista horizontal de amigos con botón eliminar
            Text("Amigos", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
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
                                    .background(Color.LightGray)
                            )
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
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar amigo",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón agregar amigo
            Button(onClick = {
                authViewModel.loadAllUsers()
                showAddFriendDialog = true
                searchQuery = ""
            }) {
                Text("Agregar amigo")
            }
        }
    }

    // Diálogo para buscar y agregar amigos
    if (showAddFriendDialog) {
        val filteredUsers = if (searchQuery.isBlank()) {
            allUsersList.filter {
                it.username.isNotBlank() &&
                        it.uid != authViewModel.currentUser?.uid &&
                        friendsList.none { friend -> friend.uid == it.uid }
            }
        } else {
            allUsersList.filter {
                it.username.isNotBlank() &&
                        it.username.contains(searchQuery, ignoreCase = true) &&
                        it.uid != authViewModel.currentUser?.uid &&
                        friendsList.none { friend -> friend.uid == it.uid }
            }
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (filteredUsers.isEmpty()) {
                        Text("No se encontraron usuarios", modifier = Modifier.padding(16.dp))
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight(0.5f)
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
                                                .background(Color.LightGray)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(user.username)
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
