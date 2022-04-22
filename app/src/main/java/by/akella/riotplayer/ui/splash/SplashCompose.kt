package by.akella.riotplayer.ui.splash

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import by.akella.riotplayer.R
import com.google.android.material.composethemeadapter.MdcTheme

@Composable
fun SplashCompose(
    viewModel: SplashComposeViewModel = viewModel(),
    navigate: () -> Unit = {}
) {
    val state by viewModel.container.stateFlow.collectAsState()
    when (state) {
        is SplashState.Decline -> AlertDialog()
        is SplashState.Scanned -> navigate()
    }

    val color = MaterialTheme.colors.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { requestResult ->
        if (requestResult) viewModel.granted() else viewModel.declined()
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = "key") {
        val result = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (!result) {
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

@Composable
private fun AlertDialog() {
    AlertDialog(
        title = @Composable { Text(text = "Error") },
        text = @Composable { Text(text = "Need permission to read music") },
        onDismissRequest = {},
        buttons = @Composable { Button(onClick = {}) { Text(text = "OK") } }
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun review() {
    MdcTheme {
        SplashCompose()
    }
}