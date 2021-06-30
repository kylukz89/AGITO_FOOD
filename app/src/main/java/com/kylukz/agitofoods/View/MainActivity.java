package com.kylukz.agitofoods.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.navigation.NavigationView;
import com.kylukz.agitofoods.DAO.EmpresaDAO;
import com.kylukz.agitofoods.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kylukz.agitofoods.Animation.Animatoo;
import com.kylukz.agitofoods.Animation.MyBounceInterpolator;
import com.kylukz.agitofoods.Animation.Sombreamento;
import com.kylukz.agitofoods.DAO.LogDAO;
import com.kylukz.agitofoods.DAO.UsuarioDAO;
import com.kylukz.agitofoods.JavaBeans.EntidadeCadastroUsuario;
import com.kylukz.agitofoods.JavaBeans.EntidadeUsuario;
import com.kylukz.agitofoods.SQLite.GeraTabelasSQLite;
import com.kylukz.agitofoods.SQLite.SQLiteGeraTabelaGerenciamento;
import com.kylukz.agitofoods.Toolbox.Connectivity;
import com.kylukz.agitofoods.Toolbox.Ferramentas;
import com.kylukz.agitofoods.Toolbox.GPSTracker;
import com.kylukz.agitofoods.Toolbox.Mask;
import com.kylukz.agitofoods.Valid.ValidaCPF;
import com.kylukz.agitofoods.Valid.ValidadorCadastro;
import com.kylukz.agitofoods.Valid.ValidadorLogin;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

/**
 * Classe de login/auth de usuários
 *
 * @author Igor Maximo
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private GoogleSignInClient mGoogleSignInClient;
    protected SQLiteGeraTabelaGerenciamento sqLiteGeraTabelaGerenciamento = new SQLiteGeraTabelaGerenciamento(MainActivity.this);

    private EntidadeUsuario entidadeUsuario = new EntidadeUsuario();
    EditText editTextCampoLogin;
    EditText editTextCampoSenha;

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    Calendar calendar = Calendar.getInstance();
    private FirebaseAuth mAuth;

    // Validação de novo usuário
    boolean primeiraVez = false; // Valida se é a primeira vez que o usuário logou com esse CPF
    boolean seCompletouCadastro = false; // Se Completou Cadastro
    private BottomSheetBehavior mBottomSheetBehavior;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); // remove barra de título
        getWindow().getDecorView();
        setContentView(R.layout.activity_main);
        try {
            // Permissão gps
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            setSolicitaGeoLocalizacao();
        } catch (Exception e) {
            System.err.println(e);
        }

        // Componentes declaração padrão
        ImageView imageView = (ImageView) findViewById(R.id.imageViewLogoMarca);
        editTextCampoLogin = (EditText) findViewById(R.id.editTextCampoLogin);
        editTextCampoSenha = (EditText) findViewById(R.id.editTextCampoSenha);
        Button buttonBotaoAcessar = (Button) findViewById(R.id.buttonBotaoAcessar);
        LinearLayout buttonBotaoAcessarGoogle = (LinearLayout) findViewById(R.id.buttonBotaoAcessarGoogle);
        LinearLayout buttonBotaoAcessarFacebook = (LinearLayout) findViewById(R.id.buttonBotaoAcessarFacebook);
        TextView textViewCreditos = (TextView) findViewById(R.id.textViewCreditos);

        //////////////////////////// SQLITE GERA DB //////////////////////////////
        GeraTabelasSQLite dbh = new GeraTabelasSQLite(MainActivity.this);
        dbh.getWritableDatabase();
        //////////////////////////////////////////////////////////////////////////


        try {
            // SQLite
            String[] infos = sqLiteGeraTabelaGerenciamento.getSelectUltimoLogin();
            if (infos[1].length() == 8 && infos[2].length() >= 5) {
                // Preenche campos com credenciais salvas
                editTextCampoLogin.setText(infos[1]);
                editTextCampoSenha.setText(infos[2]);
                try {
                    // Chama código do botão de Acessar (amarelo simples)
                    setAutenticarPorPin();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        // Desabilitando recursos informativos
        textViewCreditos.setFocusable(false);
        textViewCreditos.setEnabled(false);

        editTextCampoLogin.getBackground().setColorFilter(R.color.colorPreta, PorterDuff.Mode.SRC_IN);
        editTextCampoSenha.getBackground().setColorFilter(R.color.colorPreta, PorterDuff.Mode.SRC_IN);

        // Aplicação de efeitos de sombreamento nos componentes da tela
        Sombreamento.setSombraComponente(150.0f, 1000, 1000, buttonBotaoAcessar, R.color.colorPrimaryDark);
        //Cores
        editTextCampoLogin.getBackground().setColorFilter(R.color.colorPreta, PorterDuff.Mode.SRC_IN);
        editTextCampoSenha.getBackground().setColorFilter(R.color.colorPreta, PorterDuff.Mode.SRC_IN);

        // Auth simples
        buttonBotaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAutenticarPorPin();
            }
        });

        // Auth por Google (Gmail)
        buttonBotaoAcessarGoogle.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {


//                LayoutInflater inflater = ((Activity) MainActivity.this).getLayoutInflater();
//                View layout = inflater.inflate(R.layout.toast_msg_alerta, null);
//                TextView text = (TextView) layout.findViewById(R.id.textMsg);
//                text.setText("Em construção!");
//                Toast toast = new Toast(MainActivity.this);
//                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                toast.setDuration(Toast.LENGTH_SHORT);
//                toast.setView(layout);
//                toast.show();


                // Configure Google Sign In
//                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestIdToken(getString(R.string.default_web_client_id))
//                        .requestEmail()
//                        .build();
//                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            }
        });

        // Auth por Facebook
        buttonBotaoAcessarFacebook.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
//                LayoutInflater inflater = ((Activity) MainActivity.this).getLayoutInflater();
//                View layout = inflater.inflate(R.layout.toast_msg_alerta, null);
//                TextView text = (TextView) layout.findViewById(R.id.textMsg);
//                text.setText("Em construção!");
//                Toast toast = new Toast(MainActivity.this);
//                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                toast.setDuration(Toast.LENGTH_SHORT);
//                toast.setView(layout);
//                toast.show();
            }
        });


//////////////////////////// EFEITO NUBANK INTERPOLAÇÃO ///////////////////////////////////////////////////////
        try {
            Animatoo.animateSwipeRight(this);
            final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.enter_from_right);
            // Use bounce interpolator with amplitude 0.1 and frequency 15
            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 25);
            myAnim.setInterpolator(interpolator);
            editTextCampoLogin.startAnimation(myAnim);
        } catch (Exception e) {
            System.err.println(e);
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        // Cadastrar-se
        TextView textViewCadastrarse = (TextView) findViewById(R.id.textViewCadastrarse);
        textViewCadastrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpa
                setLimpaCampos(0);
                setExibeColapsaCompletarCadastro(0);
            }
        });

        // Esqueci a senha
        TextView textViewEsqueciSenha = (TextView) findViewById(R.id.textViewEsqueciSenha);
        textViewEsqueciSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpa
                setLimpaCampos(1);
                setExibeColapsaCompletarCadastro(1);
            }
        });


        final EditText editTextCampoNome = (EditText) findViewById(R.id.editTextCampoNome);
        final EditText editTextCampoSobrenome = (EditText) findViewById(R.id.editTextCampoSobrenome);
        final EditText editTextCampoCPF = (EditText) findViewById(R.id.editTextCampoCPF);
        final EditText editTextCampoNascimento = (EditText) findViewById(R.id.editTextCampoNascimento);
        final EditText editTextCampoCelular = (EditText) findViewById(R.id.editTextCampoCelular);
        final EditText editTextCampoEmail = (EditText) findViewById(R.id.editTextCampoEmail);
        final EditText editTextCampoSenhaCadastro = (EditText) findViewById(R.id.editTextCampoSenhaCadastro);
        editTextCampoCelular.addTextChangedListener(Mask.insert("(##) #####-####", editTextCampoCelular));
        // Botão cadastrar pin
        Button buttonBotaoSolicitarPIN = (Button) findViewById(R.id.buttonBotaoSolicitarPIN);
        // Botão esqueci pin e mandar por e-mail
        Button buttonBotaoSolicitarPINEsqueciSenhaEmail = (Button) findViewById(R.id.buttonBotaoSolicitarPINEsqueciSenhaEmail);
        final EditText editTextCampoCPFEsqueciSenha = (EditText) findViewById(R.id.editTextCampoCPFEsqueciSenha);

        // Máscaras de CPF
        editTextCampoCPFEsqueciSenha.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (editTextCampoCPFEsqueciSenha.getText().toString().length() == 11) {
                        editTextCampoCPFEsqueciSenha.setText(Ferramentas.setMascaraCPF(editTextCampoCPFEsqueciSenha.getText().toString()));
                    }
                }
            }
        });
        editTextCampoCPFEsqueciSenha.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_DEL) {
                    if (editTextCampoCPFEsqueciSenha.getText().toString().length() == 11) {
                        editTextCampoCPFEsqueciSenha.setText(Ferramentas.setMascaraCPF(editTextCampoCPFEsqueciSenha.getText().toString()));
                    }
                }
                return false;
            }
        });

        buttonBotaoSolicitarPINEsqueciSenhaEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (editTextCampoCPFEsqueciSenha.getText().length() == 14) {
                        if (ValidaCPF.valida(editTextCampoCPFEsqueciSenha.getText().toString().replace("-", "").replace(".", ""))) {
                            new AsyncTaskEsqueciSenhaUsuario().execute(editTextCampoCPFEsqueciSenha.getText().toString());
                        } else {
                            editTextCampoCPFEsqueciSenha.setError("CPF inválido!");
                        }
                    } else {
                        editTextCampoCPFEsqueciSenha.setError("CPF inválido!");
                    }
                } catch (Exception e) {

                }
            }
        });
        // Entidade
        final EntidadeCadastroUsuario entidadeCadastroUsario = new EntidadeCadastroUsuario();
        // Máscaras de CPF
        editTextCampoCPF.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (editTextCampoCPF.getText().toString().length() == 11) {
                        editTextCampoCPF.setText(Ferramentas.setMascaraCPF(editTextCampoCPF.getText().toString()));
                    }
                }
            }
        });
        editTextCampoCPF.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_DEL) {
                    if (editTextCampoCPF.getText().toString().length() == 11) {
                        editTextCampoCPF.setText(Ferramentas.setMascaraCPF(editTextCampoCPF.getText().toString()));
                    }
                }
                return false;
            }
        });
        // Pega data de nascimento
        final DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // Máscara de data
                Ferramentas.setMascaraDataPicker(calendar, MainActivity.this, R.id.editTextCampoNascimento);
            }
        };
        //Campo Data nascimento datapicker
        editTextCampoNascimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date1, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        // Set formulário
        buttonBotaoSolicitarPIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Validação
                    ValidadorCadastro validaFormularioCadastroPinPorCPF = new ValidadorCadastro();
                    // Carrega entidade
                    entidadeCadastroUsario.setNome(editTextCampoNome.getText().toString());
                    entidadeCadastroUsario.setSobreNome(editTextCampoSobrenome.getText().toString());
                    entidadeCadastroUsario.setCpf(editTextCampoCPF.getText().toString());
                    entidadeCadastroUsario.setDataNascimento(editTextCampoNascimento.getText().toString());
                    entidadeCadastroUsario.setCelular(editTextCampoCelular.getText().toString());
                    entidadeCadastroUsario.setEmail(editTextCampoEmail.getText().toString());
                    entidadeCadastroUsario.setSenha(editTextCampoSenhaCadastro.getText().toString());
                    // Set validar
                    Object[] valida = new Object[3];
                    valida = validaFormularioCadastroPinPorCPF.setValidaFormularioCadastroPinPorCPF(entidadeCadastroUsario);
                    // Nome
                    if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 0) {
                        System.out.println("====================> 1");
                        editTextCampoNome.setError(valida[1] + "");
                        return;
                    }
                    // Sobrenome
                    if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 1) {
                        System.out.println("====================> 2");
                        editTextCampoSobrenome.setError(valida[1] + "");
                        return;
                    }
                    // CPF
                    if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 2) {
                        System.out.println("====================> 3");
                        editTextCampoCPF.setError(valida[1] + "");
                        return;
                    }
                    // Nascimento
                    if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 3) {
                        System.out.println("====================> 4");
                        editTextCampoNascimento.setError(valida[1] + "");
                        return;
                    }
                    // Celular
                    if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 4) {
                        System.out.println("====================> 5");
                        editTextCampoCelular.setError(valida[1] + "");
                        return;
                    }
                    // E-mail
                    if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 5) {
                        System.out.println("====================> 6");
                        editTextCampoEmail.setError(valida[1] + "");
                        return;
                    }
                    // Senha
                    if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 6) {
                        System.out.println("====================> 7");
                        editTextCampoSenhaCadastro.setError(valida[1] + "");
                        return;
                    }
                    // DAO
                    new AsyncTaskSetCadastrarUsuario().execute(entidadeCadastroUsario);
                } catch (Exception e) {

                }
            }
        });

        ////////////////////////////////////
        //       COMPLETAR CADASTRO       //
        ////////////////////////////////////
        final EditText editTextCampoCEPCompletarCadastro = (EditText) findViewById(R.id.editTextCampoCEPCompletarCadastro);
        final EditText editTextCampoEnderecoCompletarCadastro = (EditText) findViewById(R.id.editTextCampoEnderecoCompletarCadastro);
        final EditText editTextCampoNumero = (EditText) findViewById(R.id.editTextCampoNumero);
        final EditText editTextCampoBairro = (EditText) findViewById(R.id.editTextCampoBairro);
        final EditText editTextCampoCidade = (EditText) findViewById(R.id.editTextCampoCidade);
        final EditText editTextCampoReferencia = (EditText) findViewById(R.id.editTextCampoReferencia);
        // RequestFocus para eficiência
        editTextCampoCEPCompletarCadastro.requestFocus();
        // Botão completar
        Button buttonBotaoCompletarCadastro = (Button) findViewById(R.id.buttonBotaoCompletarCadastro);
        buttonBotaoCompletarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // permissão
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    if (setAlertaGPSOnOff()) {
                        // Get geo
                        Object[] geo = setSolicitaGeoLocalizacao();
                        // Validação
                        ValidadorCadastro validaFormularioCadastroPinPorCPF = new ValidadorCadastro();
                        // Carrega entidade
                        entidadeCadastroUsario.setCep(editTextCampoCEPCompletarCadastro.getText().toString());
                        entidadeCadastroUsario.setEndereco(editTextCampoEnderecoCompletarCadastro.getText().toString());
                        entidadeCadastroUsario.setNumero(editTextCampoNumero.getText().toString());
                        entidadeCadastroUsario.setBairro(editTextCampoBairro.getText().toString());
                        entidadeCadastroUsario.setCidade(editTextCampoCidade.getText().toString());
                        entidadeCadastroUsario.setLatitude(geo[1].toString());
                        entidadeCadastroUsario.setLongitude(geo[0].toString());
                        entidadeCadastroUsario.setReferencia(editTextCampoReferencia.getText().toString());

                        // Set validar
                        Object[] valida = null;
                        valida = validaFormularioCadastroPinPorCPF.setValidaFormularioCompletarCadastro(entidadeCadastroUsario);
                        // CEP
                        if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 0) {
                            System.out.println("====================> 1");
                            editTextCampoCEPCompletarCadastro.setError(valida[1] + "");
                            return;
                        }
                        // Endereco
                        if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 1) {
                            System.out.println("====================> 2");
                            editTextCampoEnderecoCompletarCadastro.setError(valida[1] + "");
                            return;
                        }
                        // nº
                        if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 2) {
                            System.out.println("====================> 3");
                            editTextCampoNumero.setError(valida[1] + "");
                            return;
                        }
                        // Bairro
                        if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 3) {
                            System.out.println("====================> 4");
                            editTextCampoBairro.setError(valida[1] + "");
                            return;
                        }
                        // Cidade
                        if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 4) {
                            System.out.println("====================> 5");
                            editTextCampoCidade.setError(valida[1] + "");
                            return;
                        }

                        // DAO
                        new MainActivity.AsyncTaskSetCompletarCadastro().execute(entidadeCadastroUsario);
                    }
                } catch (Exception e) {
                    System.out.println("====================> 5" + e);
                }
            }
        });

        // Campo endereço por CEP - Completar cadastro
        editTextCampoCEPCompletarCadastro.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                try {
                    if (editTextCampoCEPCompletarCadastro.getText().toString().length() == 8) {
                        JSONObject dadosRuaCEP = Ferramentas.getEnderecoPorCEP(editTextCampoCEPCompletarCadastro.getText().toString());
                        editTextCampoEnderecoCompletarCadastro.setText(dadosRuaCEP.getString("logradouro"));
                        editTextCampoBairro.setText(dadosRuaCEP.getString("bairro"));
                        editTextCampoCidade.setText(dadosRuaCEP.getString("localidade"));
                        // Focus nº endereço
                        editTextCampoNumero.requestFocus();
                    }
                } catch (Exception e) {
                    System.out.println("e ====================> " + e);
                }
                return false;
            }
        });

    }

    /**
     * Limpa campos de cadastro
     *
     * @author Igor Maximo
     * @date 18/06/2020
     */
    private void setLimpaCampos(int layoutBottomSheet) {
        switch (layoutBottomSheet) {
            case 0:
                // Novo Cadastro
                final EditText editTextCampoNome = (EditText) findViewById(R.id.editTextCampoNome);
                final EditText editTextCampoSobrenome = (EditText) findViewById(R.id.editTextCampoSobrenome);
                final EditText editTextCampoCPF = (EditText) findViewById(R.id.editTextCampoCPF);
                final EditText editTextCampoNascimento = (EditText) findViewById(R.id.editTextCampoNascimento);
                final EditText editTextCampoCelular = (EditText) findViewById(R.id.editTextCampoCelular);
                final EditText editTextCampoEmail = (EditText) findViewById(R.id.editTextCampoEmail);
                final EditText editTextCampoSenhaCadastro = (EditText) findViewById(R.id.editTextCampoSenhaCadastro);
                editTextCampoNome.setText("");
                editTextCampoSobrenome.setText("");
                editTextCampoCPF.setText("");
                editTextCampoNascimento.setText("");
                editTextCampoCelular.setText("");
                editTextCampoEmail.setText("");
                editTextCampoSenhaCadastro.setText("");
                break;
            case 1:
                // Esqueci senha
                final EditText editTextCampoCPFEsqueciSenha = (EditText) findViewById(R.id.editTextCampoCPFEsqueciSenha);
                editTextCampoCPFEsqueciSenha.setText("");
                break;
        }
    }

    private boolean setAlertaGPSOnOff() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(this);
            dialog.setMessage("GPS está desligado, favor ligá-lo!");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);

                }
            });
            androidx.appcompat.app.AlertDialog alert = dialog.create();
            alert.show();
        }
        return gps_enabled;

//        final boolean[] ligado = {false};
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setMessage("O serviço de GPS é necessário e está desligado no momento, deseja ligá-lo?")
//                .setCancelable(false)
//                .setPositiveButton("Página de configurações",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                Intent callGPSSettingIntent = new Intent(
//                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                                startActivity(callGPSSettingIntent);
//                                ligado[0] = true;
//                            }
//                        });
//        alertDialogBuilder.setNegativeButton("Cancelar",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        ligado[0] = false;
//                        dialog.cancel();
//
//                    }
//                });
//        AlertDialog alert = alertDialogBuilder.create();
//        alert.show();
//        return ligado[0];
    }


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // [START_EXCLUDE silent]
//        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
//        if (user != null) {
//            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
//
//            findViewById(R.id.signInButton).setVisibility(View.GONE);
//            findViewById(R.id.signOutAndDisconnect).setVisibility(View.VISIBLE);
//        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);
//
//            findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
//            findViewById(R.id.signOutAndDisconnect).setVisibility(View.GONE);
//        }
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }


    /**
     * Altera o state da bottom sheet
     *
     * @author Igor Maximo
     * @date 01/05/2020
     */
    private void setExpandeColapsaBottomSheet(int state, int bottomSheetAcao) {
        View bottomSheet = null;
        switch (bottomSheetAcao) {
            case 0:
                bottomSheet = findViewById(R.id.bottom_sheet_cadastra_pin);
                break;
            case 1:
                bottomSheet = findViewById(R.id.bottom_sheet_esqueci_senha);
                break;
            case 2:
                bottomSheet = findViewById(R.id.bottom_sheet_completar_cadastro_endereco);
                break;
        }
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(state);
        bottomSheet.requestLayout();
        bottomSheet.invalidate();
    }

    /**
     * Exibe a roleta de produtos
     */
    public void setExibeColapsaCompletarCadastro(int bottomSheetAcao) {
        try {
            View bottomSheet = null;
            switch (bottomSheetAcao) {
                case 0:
                    bottomSheet = findViewById(R.id.bottom_sheet_cadastra_pin);
                    break;
                case 1:
                    bottomSheet = findViewById(R.id.bottom_sheet_esqueci_senha);
                    break;
            }
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            switch (mBottomSheetBehavior.getState()) {
                case BottomSheetBehavior.STATE_COLLAPSED:
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    bottomSheet.requestLayout();
                    bottomSheet.invalidate();
                    break;
                case BottomSheetBehavior.STATE_DRAGGING:
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    bottomSheet.requestLayout();
                    bottomSheet.invalidate();
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Método do botão "Acessar" simples
     *
     * @author Igor Maximo
     * @date 26/07/2020
     */
    private void setAutenticarPorPin() {
        try {
            ValidadorLogin validadorLogin = new ValidadorLogin();
            // Set validar
            Object[] valida = new Object[3];
            valida = validadorLogin.setValidaFormularioLoginPorPin(editTextCampoLogin.getText().toString(), editTextCampoSenha.getText().toString());
            // Validação
            if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 0) {
                editTextCampoLogin.setError(valida[1] + "");
                return;
            }
            if (!Boolean.parseBoolean(String.valueOf(valida[0])) && Integer.parseInt(String.valueOf(valida[2])) == 1) {
                editTextCampoSenha.setError(valida[1] + "");
                return;
            }
            // Auth
            if (Connectivity.isConnected(MainActivity.this)) {
                new AsyncTaskGetUsuario().execute(editTextCampoLogin.getText().toString(), editTextCampoSenha.getText().toString()); // Realiza consulta no servidor quando for autenticação por PIN
            } else {
                setGeraPopUpAlertaMsg("Não há conexão!", MainActivity.this, R.layout.toast_msg_alerta, true);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LogDAO.setErro("MainActivity", "buttonBotaoAcessar", e.getMessage(), 81, 0, new EntidadeUsuario().getId(), new EntidadeUsuario().getFkVersionamento());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        dumpLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * AsyncTask para o carregamento dos dados
     * do usuário
     *
     * @author Igor Maximo
     * @data 09/05/2020
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskGetUsuario extends AsyncTask<String, Integer, EntidadeUsuario> {
        private ProgressDialog mProgress = null;

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected EntidadeUsuario doInBackground(String... strings) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new UsuarioDAO().getUsuario(strings[0], strings[1]);
        }

        @Override
        protected void onPreExecute() {
            this.mProgress = ProgressDialog.show(MainActivity.this, null, "Processando...", true);
            this.mProgress.setIndeterminate(false);
            this.mProgress.setCancelable(false);
        }

        @Override
        protected void onPostExecute(EntidadeUsuario result) {
            UsuarioDAO.ENTIDADE_USUARIO = result;
            setLogar(result);
            mProgress.dismiss();
            super.onPostExecute(result);
        }
    }

    /**
     * Exibe o "modal" para mostrar uma mensagem de aviso qualquer
     *
     * @author Igor Maximo
     * @data 18/04/2019
     */
    public void setGeraPopUpAlertaMsg(String msg, Context context, int layoutToastMsg, boolean seLongoTempo) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(layoutToastMsg, (ViewGroup) ((Activity) context).findViewById(R.id.toast_layout_root_autorizada));
        TextView text = (TextView) layout.findViewById(R.id.textMsg);
        text.setText(msg);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        if (seLongoTempo) {
            toast.setDuration(Toast.LENGTH_LONG);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.setView(layout);
        toast.show();
    }

    /**
     * AsyncTask para o cadastrar
     * o PIN de um novo usuário
     *
     * @author Igor Maximo
     * @data 17/06/2020
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskSetCadastrarUsuario extends AsyncTask<EntidadeCadastroUsuario, Integer, Object[]> {
        private ProgressDialog mProgress = null;

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected Object[] doInBackground(EntidadeCadastroUsuario... entidadeCadastroUsuario) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // DAO
            return new UsuarioDAO().setCadastrarUsuario(entidadeCadastroUsuario[0]);
        }

        @Override
        protected void onPreExecute() {
            try {
                this.mProgress = ProgressDialog.show(MainActivity.this, null, "Processando...", true);
                this.mProgress.setIndeterminate(false);
                this.mProgress.setCancelable(false);
            } catch (Exception ignored) {
                // do nothing, activity keeps working
            }
        }

        @Override
        protected void onPostExecute(Object[] result) {
            mProgress.dismiss();
            if (Boolean.parseBoolean(result[0] + "")) {
                setGeraPopUpAlertaMsg(result[1] + "", MainActivity.this, R.layout.toast_msg_sucesso, false);
                // Esconde bottomsheet
                setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_COLLAPSED, 0);
                // Limpa
                setLimpaCampos(0);
            } else {
                setGeraPopUpAlertaMsg(result[1] + "", MainActivity.this, R.layout.toast_msg_erro, true);
            }
            super.onPostExecute(result);
        }
    }

    /**
     * AsyncTask para o cadastrar
     * o PIN de um novo usuário
     *
     * @author Igor Maximo
     * @data 17/06/2020
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskEsqueciSenhaUsuario extends AsyncTask<String, Integer, Object[]> {
        private ProgressDialog mProgress = null;

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected Object[] doInBackground(String... cpf) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // DAO
            return new UsuarioDAO().setEnviarPINUsuarioPorEmail(cpf[0]);
        }

        @Override
        protected void onPreExecute() {
            this.mProgress = ProgressDialog.show(MainActivity.this, null, "Processando...", true);
            this.mProgress.setIndeterminate(false);
            this.mProgress.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Object[] result) {
            mProgress.dismiss();
            if (Boolean.parseBoolean(result[0] + "")) {
                setGeraPopUpAlertaMsg(result[1] + "", MainActivity.this, R.layout.toast_msg_sucesso, false);
                // Esconde bottomsheet
                setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_COLLAPSED, 1);
                // Limpa
                setLimpaCampos(1);
            } else {
                setGeraPopUpAlertaMsg(result[1] + "", MainActivity.this, R.layout.toast_msg_erro, true);
            }
            super.onPostExecute(result);
        }
    }

    /**
     * Método que autentica o usuário e armazenar
     * os dados para uso posterior em toda a aplicação
     *
     * @param entidadeUsuario
     * @author Igor Maximo
     * @date 12/05/2020
     */
    public void setLogar(EntidadeUsuario entidadeUsuario) {
        try {
            // Se autenticação por PIN
            if (entidadeUsuario.isSeAuth()) {
                if (entidadeUsuario.getPin().equals(editTextCampoLogin.getText().toString())
                        && entidadeUsuario.getSenha().equals(editTextCampoSenha.getText().toString()) && entidadeUsuario.getPin() != null && entidadeUsuario.getSenha() != null) {
                    ///////// SQLITE /////////
                    sqLiteGeraTabelaGerenciamento.setAtualizaLogin(entidadeUsuario.getPin(), entidadeUsuario.getSenha(), "PIN");
                    // Carrega tela de lista de empresas
                    //Intent intenetAuthPrincipal = new Intent(MainActivity.this, MenuPrincipalEmpresa.class);
                    // Carrega apenas uma empresa diretamente (AGITO FOODS)

                    EmpresaDAO empresaDAO = new EmpresaDAO();
                    // Set empresa escolhida
                    EmpresaActivity.ENTIDADE_EMPRESA = empresaDAO.getListaEmpresa().get(0); // Seta empresa escolhida
                    EmpresaActivity.FK_EMPRESA_SELECIONADA = 1;

                    // Se o usuário completou o cadastro
                    seCompletouCadastro = UsuarioDAO.ENTIDADE_USUARIO.isSeCompletouCadastro();
                    if (primeiraVez && seCompletouCadastro) {
                        primeiraVez = false;
                    }
                    primeiraVez = true;
                    // Se não completou o cadastro
                    if (!seCompletouCadastro) {
                        setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_EXPANDED, 2);
                        View bottomSheet = findViewById(R.id.bottom_sheet_completar_cadastro_endereco);
                        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

                        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                            @Override
                            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                                if (!seCompletouCadastro && (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED)) {
                                    setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_EXPANDED, 2);
                                }
                            }

                            @Override
                            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                                //
                            }
                        });
                    }

                    if (seCompletouCadastro) {
                        Intent intenetAuthPrincipal = new Intent(MainActivity.this, EmpresaActivity.class);
                        startActivity(intenetAuthPrincipal);
                        finish();
                    }
                } else {
                    setGeraPopUpAlertaMsg(entidadeUsuario.getMsgAuth() + "", MainActivity.this, R.layout.toast_msg_erro, true);
                }
            } else {
                if (entidadeUsuario.getMsgAuth() == null) {
                    setGeraPopUpAlertaMsg("Erro interno!", MainActivity.this, R.layout.toast_msg_erro, true);
                } else {
                    setGeraPopUpAlertaMsg(entidadeUsuario.getMsgAuth() + "", MainActivity.this, R.layout.toast_msg_erro, true);
                }
            }
        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void dumpProviders(String s) {

    }

    private void dumpProviders() {

    }

    private void dumpLocation(Location l) {

    }


    /**
     * Ponte de escuta para as consultas de geolocalização
     *
     * @author Igor Maximo
     * @date 17/10/2019
     * @updated 17/10/2019
     */
    public Object[] setSolicitaGeoLocalizacao() {
        // instancia o service, GPSTracker gps
        GPSTracker gps = new GPSTracker(MainActivity.this);
        double latitude = 0.0;
        double longitude = 0.0;
        // verifica ele
        if (gps.canGetLocation()) {
            // passa sua latitude e longitude para duas variaveis
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }
        System.err.println("GPS==> " + longitude + ", " + latitude);
        return new Object[]{longitude, latitude};
    }

    /**
     * AsyncTask para o cadastrar
     * concluir o cadastro do cliente
     *
     * @author Igor Maximo
     * @data 20/06/2020
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskSetCompletarCadastro extends AsyncTask<EntidadeCadastroUsuario, Integer, Object[]> {
        private ProgressDialog mProgress = null;

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected Object[] doInBackground(EntidadeCadastroUsuario... entidadeCadastroUsuario) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // DAO
            return new UsuarioDAO().setCompletarCadastroUsuario(entidadeCadastroUsuario[0]);
        }

        @Override
        protected void onPreExecute() {
            this.mProgress = ProgressDialog.show(MainActivity.this, null, "Processando...", true);
            this.mProgress.setIndeterminate(false);
            this.mProgress.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Object[] result) {
            mProgress.dismiss();
            if (Boolean.parseBoolean(result[0] + "")) {
                setGeraPopUpAlertaMsg(result[1] + "", MainActivity.this, R.layout.toast_msg_sucesso, false);
                // Habilita layoout
                setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_EXPANDED, 2);
                // Se completou cadastro
                seCompletouCadastro = true;
                // Esconde bottomshet completar cadastro
                setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_COLLAPSED, 2);
                // Aciona o botão de logar
                Button buttonBotaoAcessar = (Button) findViewById(R.id.buttonBotaoAcessar);
                buttonBotaoAcessar.callOnClick();
            } else {
                setGeraPopUpAlertaMsg(result[1] + "", MainActivity.this, R.layout.toast_msg_erro, true);
            }
            super.onPostExecute(result);
        }
    }
}