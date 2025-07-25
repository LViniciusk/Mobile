package com.example.mygamelist.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mygamelist.R
import com.example.mygamelist.ui.theme.ThemePreference
import com.example.mygamelist.viewmodel.ThemeViewModel
import com.example.mygamelist.viewmodel.ProfileViewModel
import com.example.mygamelist.viewmodel.ProfileUiEvent
import com.example.mygamelist.viewmodel.AuthViewModel
import com.example.mygamelist.viewmodel.AuthUiEvent
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(tab: Int, themeViewModel: ThemeViewModel) {
    var selectedTabIndex by remember(tab) { mutableIntStateOf(tab) }
    val tabs = listOf("Perfil", "Configurações")

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> ProfilePageUi()
                1 -> ConfigurationPageUi(themeViewModel = themeViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePageUi(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onProfileImageSelected(uri)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Carregando perfil...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset(y = 40.dp)
            )
        }
        return
    }

    if (uiState.user == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = uiState.error ?: "Perfil não carregado. Faça login novamente.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Button(onClick = { viewModel.reloadData() }) {
                Text("Tentar Novamente")
            }
        }
        return
    }

    val user = uiState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(98.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { pickImageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageUrlToDisplay = uiState.selectedProfileImageUri?.toString() ?: (user?.profileImageUrl
                    ?: "https://cdn3.iconfinder.com/data/icons/flat-pro-user-management-set-4/32/user-profile-512.png")

                AsyncImage(
                    model = imageUrlToDisplay,
                    contentDescription = "Foto do perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.avatar_placeholder),
                    error = painterResource(id = R.drawable.avatar_placeholder)
                )
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recomenda-se usar uma imagem com pelo menos 98x98 pixels e 2MB ou menos. Use um arquivo PNG ou JPG.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { pickImageLauncher.launch("image/*") }) {
                    Text("Alterar imagem")
                }
                TextButton(onClick = { viewModel.onProfileImageSelected(null) }) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Divider(color = MaterialTheme.colorScheme.outline)

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Nome de usuário", style = MaterialTheme.typography.labelLarge)
            Text(
                user?.username ?: "username",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Seu nome de usuário é exclusivo para sua conta e aparece na sua página de perfil e na sua URL. Não é o seu endereço de email.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline)

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Nome de exibição", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text("Seu nome de exibição") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                "É separado de seu nome de usuário, pode ser seu nick de qualquer jogo, nome comercial ou seu nome real e é mostrado ao lado do seu nome de usuário.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline)

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Bio", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = uiState.bio,
                onValueChange = viewModel::onBioChange,
                label = { Text("Sua bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(top = 4.dp, bottom = 4.dp),
                singleLine = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                "Algo sobre você em menos de 85 caracteres.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = viewModel::resetProfile,
                enabled = !uiState.isSavingProfile
            ) {
                Text("Redefinir")
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = viewModel::saveProfile,
                enabled = !uiState.isSavingProfile
            ) {
                if (uiState.isSavingProfile) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salvar")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationPageUi(themeViewModel: ThemeViewModel, authViewModel: AuthViewModel = hiltViewModel()) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val currentThemePreference by themeViewModel.themePreference.collectAsState()
    val isEffectivelyDark = when (currentThemePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    LaunchedEffect(key1 = Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                AuthUiEvent.ReauthenticateRequired -> {
                    Toast.makeText(context, "Por favor, faça login novamente para realizar esta ação.", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column {
            Text("Email", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(uiState.currentUserEmail, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verificado",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Email verificado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)


        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Alterar Senha", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            PasswordTextFieldUi(
                label = "Senha atual",
                password = uiState.currentPasswordInput,
                onPasswordChange = authViewModel::onCurrentPasswordInputChange,
                isVisible = uiState.isChangingPassword,
                onVisibilityChange = {  }
            )

            Text(
                text = "A senha deve ter 8+ caracteres com letras maiúsculas, minúsculas, números e caracteres especiais. Evite informações pessoais e padrões comuns. Use senhas únicas para cada conta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            PasswordTextFieldUi(
                label = "Nova senha",
                password = uiState.newPasswordInput,
                onPasswordChange = authViewModel::onNewPasswordInputChange,
                isVisible = uiState.isChangingPassword,
                onVisibilityChange = {  }
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline)

            PasswordTextFieldUi(
                label = "Confirmar nova senha",
                password = uiState.confirmNewPasswordInput,
                onPasswordChange = authViewModel::onConfirmNewPasswordInputChange,
                isVisible = uiState.isChangingPassword,
                onVisibilityChange = {  }
            )
        }

        uiState.changePasswordError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = authViewModel::changePassword,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            enabled = !uiState.isChangingPassword
        ) {
            if (uiState.isChangingPassword) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Atualizar senha", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)

        Column {
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Modo Escuro", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isEffectivelyDark,
                    onCheckedChange = { isChecked ->
                        themeViewModel.setThemePreference(
                            if (isChecked) ThemePreference.DARK else ThemePreference.LIGHT
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = { themeViewModel.setThemePreference(ThemePreference.SYSTEM) },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Usar padrão do sistema")
            }
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))


        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Excluir Conta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "A exclusão da conta é permanente e removerá todos os seus dados. Esta ação não pode ser desfeita.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            uiState.deleteAccountError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
            Button(
                onClick = authViewModel::deleteAccount,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                enabled = !uiState.isDeletingAccount
            ) {
                if (uiState.isDeletingAccount) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                } else {
                    Text("Excluir conta")
                }
            }
        }
    }
}


@Composable
fun PasswordTextFieldUi(
    label: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}