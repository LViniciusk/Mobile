package com.example.mygamelist.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygamelist.ui.theme.ThemePreference
import com.example.mygamelist.viewmodel.ThemeViewModel


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

@Composable
fun ProfilePageUi() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Editar Perfil",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Nome de Usuário") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Citação") },
            modifier = Modifier.fillMaxWidth()
        )

    }
}

@Composable
fun ConfigurationPageUi(themeViewModel: ThemeViewModel) {
    val email by remember { mutableStateOf("MyGameList@gmail.com") }
    val isEmailVerified by remember { mutableStateOf(true) }

    var currentPassword by remember { mutableStateOf("") }
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPassword by remember { mutableStateOf("") }
    var isConfirmNewPasswordVisible by remember { mutableStateOf(false) }


    val currentThemePreference by themeViewModel.themePreference.collectAsState()

    val isEffectivelyDark = when (currentThemePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
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
            Text(email, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            if (isEmailVerified) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Verificado",
                        tint = Color(0xFF2E2E2E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Email verificado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E2E2E)
                    )
                }
            }
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))


        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Alterar Senha", style = MaterialTheme.typography.titleMedium)

            PasswordTextFieldUi(
                label = "Senha atual",
                password = currentPassword,
                onPasswordChange = { currentPassword = it },
                isVisible = isCurrentPasswordVisible,
                onVisibilityChange = { isCurrentPasswordVisible = !isCurrentPasswordVisible }
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
                password = newPassword,
                onPasswordChange = { newPassword = it },
                isVisible = isNewPasswordVisible,
                onVisibilityChange = { isNewPasswordVisible = !isNewPasswordVisible }
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            PasswordTextFieldUi(
                label = "Confirmar nova senha",
                password = confirmNewPassword,
                onPasswordChange = { confirmNewPassword = it },
                isVisible = isConfirmNewPasswordVisible,
                onVisibilityChange = { isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible }
            )
        }

        Button(
            onClick = { /* lalala */ },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("Atualizar senha")
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))


        Column {
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
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

        // Seção de Ações da Conta
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { /* lalala */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Text("Excluir conta")
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
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (isVisible) "Esconder senha" else "Mostrar senha"
            IconButton(onClick = onVisibilityChange) {
                Icon(imageVector = image, contentDescription = description)
            }
        }
    )
}

