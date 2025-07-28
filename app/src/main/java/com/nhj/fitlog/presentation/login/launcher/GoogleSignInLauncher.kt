package com.nhj.fitlog.presentation.login.launcher

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.nhj.fitlog.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException

@Composable
fun rememberGoogleSignInLauncher(
    onSuccess: (GoogleSignInAccount) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val client = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            task.getResult(ApiException::class.java)?.let(onSuccess)
        } catch (_: ApiException) { }
    }

    return { launcher.launch(client.signInIntent) }
}