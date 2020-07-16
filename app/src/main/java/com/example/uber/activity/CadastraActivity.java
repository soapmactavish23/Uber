package com.example.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.example.uber.R;
import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.Base64Custom;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastraActivity extends AppCompatActivity {

    private TextInputEditText editNome, editEmail, editSenha;
    private Switch switchTipoUsuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastra);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        switchTipoUsuario = findViewById(R.id.switch3);
    }

    public void validarCadastroUsuario(View view){
        //Recuperar textos dos campos
        String nome = editNome.getText().toString();
        String email = editEmail.getText().toString();
        String senha = editSenha.getText().toString();

        if(!nome.isEmpty()){
            if(!email.isEmpty()){
                if(!senha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setNome(nome);
                    usuario.setEmail(email);
                    usuario.setSenha(senha);
                    usuario.setTipo(verificaTipoUsuario());
                    cadastrarUsuario(usuario);

                }else{
                    Toast.makeText(
                            getApplicationContext(),
                            "Digite sua Senha",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }else{
                Toast.makeText(
                        getApplicationContext(),
                        "Digite seu E-mail",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Digite seu Nome",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public String verificaTipoUsuario(){
        return switchTipoUsuario.isChecked() ? "M" : "P";
    }

    public void cadastrarUsuario(final Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try{
                        String id = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId(id);
                        usuario.salvar();
                        //Salvar dados profile do firebase
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        if(verificaTipoUsuario() == "P"){
                            startActivity(new Intent(CadastraActivity.this, PassageiroActivity.class));
                            finish();
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Sucesso ao cadastrar passageiro",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }else{
                            startActivity(new Intent(CadastraActivity.this, RequisicoesActivity.class));
                            finish();
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Sucesso ao cadastrar motorista",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch ( FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch ( FirebaseAuthInvalidCredentialsException e){
                        excecao= "Por favor, digite um e-mail válido";
                    }catch ( FirebaseAuthUserCollisionException e){
                        excecao = "Este conta já foi cadastrada";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: "  + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(
                            CadastraActivity.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}