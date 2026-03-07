package com.example.letscontinue

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ChatListSr (
    onLogout:()-> Unit,
    onOpenChat: (String) -> Unit
){
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var myReferralCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val currentUid = auth.currentUser?.uid ?: return@LaunchedEffect

        // Get my referral code
        firestore.collection("users")
            .document(currentUid)
            .get()
            .addOnSuccessListener { myDoc ->

                val code = myDoc.getString("referralCode")
                myReferralCode = code ?: ""
            }

        // Load users who used my referral code
        firestore.collection("users")
            .whereEqualTo("referredBy", currentUid)
            .addSnapshotListener { documents, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val users = documents?.map { doc ->
                    User(
                        uid = doc.id,
                        email = doc.getString("email") ?: "",
                        name = doc.getString("name") ?: "User"
                    )
                } ?: emptyList()

                userList = users
            }
    }
    Scaffold (
        floatingActionButton = {
            FloatingActionButton(
                onClick = {/*new chat*/ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        bottomBar = {
            BottomNavigationBar()
        }
    ){ padding ->

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ){
            TopSection(
                onLogout = onLogout,
                referralCode = myReferralCode

            )
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar()
            Spacer(modifier = Modifier.height(16.dp))
            FilterChips()
            Spacer(modifier = Modifier.height(16.dp))
            ChatList( userList = userList,
                onUserClick = { user ->
                    onOpenChat(user.uid)
                }
            )
        }
    }
}

fun saveToFirestore(field: String,value: String){
    val uid= FirebaseAuth.getInstance().currentUser?.uid?:return

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .update(field,value)
}
@Composable
fun TopSection(
    onLogout:()-> Unit,
    referralCode: String
){

    var expanded by remember { mutableStateOf(false) }
    var  isEditingName by remember { mutableStateOf(false) }
    var isEditingBio by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("User Name") }
    var bio by remember { mutableStateOf("this is user bio") }

    val snackbarHostState=remember { SnackbarHostState() }
    val scope= rememberCoroutineScope()
    LaunchedEffect(Unit) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                name = document.getString("name") ?: ""
                bio = document.getString("bio") ?: ""
            }
    }
    Row(
        modifier= Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Box{
            IconButton(onClick = {expanded=true}){
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile"
                )
            }

            DropdownMenu(
                expanded=expanded,
                onDismissRequest = {expanded=false}
            ) {
                Column (
                    modifier = Modifier.padding(16.dp).width(260.dp)
                ){
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)){
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit photo"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditingName) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        saveToFirestore("name", name)
                                        isEditingName = false
                                    }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = { isEditingName = true }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name")
                            }
                        }
                    }
                    if (isEditingBio) {

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        saveToFirestore("bio", bio)
                                        isEditingBio = false
                                    }
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        )

                    } else {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = bio,
                                color = Color.Gray
                            )

                            IconButton(
                                onClick = { isEditingBio = true }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Bio")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Referral Code",
                        style=MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row (
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = referralCode,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val clipboard = LocalClipboardManager.current

                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(referralCode))
                                scope.launch {
                                    snackbarHostState.showSnackbar("Referral code copied")
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "copy")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            expanded = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search conversations") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        }
    )
}

@Composable
fun FilterChips(){
    var selected by remember { mutableStateOf("All") }
    Row (
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ){
        listOf("All" ,"Unread","Archived").forEach { label->
            FilterChip(
                selected =selected == label,
                onClick = { selected = label },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun ChatList(
    userList: List<User>,
    onUserClick: (User) -> Unit
){


    LazyColumn {
        items(userList){user ->
            Row (
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable{onUserClick(user)},
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = painterResource(R.drawable.outline_account_circle_24),
                    contentDescription = "profile",
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(user.name,
                        maxLines =1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {Icon(Icons.AutoMirrored.Filled.Chat,null)},
            label={Text("Chats")}
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Contacts") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Call, null) },
            label = { Text("Calls") }
        )
    }
}
