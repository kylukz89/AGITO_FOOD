package com.kylukz.agitofoods.View;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kylukz.agitofoods.DAO.UsuarioDAO;
import com.kylukz.agitofoods.JavaBeans.EntidadeCadastroUsuario;
import com.kylukz.agitofoods.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.circularreveal.CircularRevealGridLayout;
import com.kylukz.agitofoods.Adapters.BotoesMenuPontoAdapter;
import com.kylukz.agitofoods.Adapters.ComandaAdapter;
import com.kylukz.agitofoods.Adapters.EscolhaFormasPagamentoAdapter;
import com.kylukz.agitofoods.Adapters.ProdutoGrupoEscolhidoAdapter;
import com.kylukz.agitofoods.Adapters.GruposEmpresaAdapter;
import com.kylukz.agitofoods.Animation.Animatoo;
import com.kylukz.agitofoods.Cart.AtributoCarrinho;
import com.kylukz.agitofoods.Cart.Carrinho;
import com.kylukz.agitofoods.DAO.GruposEmpresaDAO;
import com.kylukz.agitofoods.DAO.ProdutoDAO;
import com.kylukz.agitofoods.JavaBeans.EntidadeBotoesMenuPonto;
import com.kylukz.agitofoods.JavaBeans.EntidadeCarrinhoPonto;
import com.kylukz.agitofoods.JavaBeans.EntidadeEmpresa;
import com.kylukz.agitofoods.JavaBeans.EntidadeGrupoProdutosEmpresa;
import com.kylukz.agitofoods.JavaBeans.EntidadePagamentoBandeirasCartao;
import com.kylukz.agitofoods.JavaBeans.EntidadePagamentoCartaoCredito;
import com.kylukz.agitofoods.JavaBeans.EntidadePagamentoEscolhaModo;
import com.kylukz.agitofoods.JavaBeans.EntidadeProdutosGrupoEscolhido;
import com.kylukz.agitofoods.Toolbox.Ferramentas;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kylukz.agitofoods.Toolbox.RecyclerItemClickListener;
import com.kylukz.agitofoods.Toolbox.VariaveisGlobais;
import com.kylukz.agitofoods.Valid.ValidadorCadastro;

import java.util.ArrayList;
import java.util.List;

public class EmpresaActivity extends FragmentActivity {

    public static int FK_EMPRESA_SELECIONADA;  // Empresa selecionada
    public static int FK_GRUPO_PRODUTO_ESCOLHIDO; // Grupo de produto
    private static int POSITION_ROLETA_BOTOES;

    public static EntidadeEmpresa ENTIDADE_EMPRESA; // Entidade Empresa
    public volatile static List<EntidadeGrupoProdutosEmpresa> listaEntidadeGrupoProdutosEmpresa;
    public volatile static List<EntidadeProdutosGrupoEscolhido> listaEntidadeProdutosGrupoEscolhido;
    private RecyclerView.Adapter adapter;
    public static Context CTX;
    // Entidade
    final EntidadeCadastroUsuario entidadeCadastroUsario = new EntidadeCadastroUsuario();
    UsuarioDAO usuarioDAO = new UsuarioDAO();

    private LocationManager mgr;
    private String best;
    public static double myLocationLatitude;
    public static double myLocationLongitude;
    // Armazena o token do firebase
    public volatile static String TOKEN_FIREBASE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView();
        setContentView(R.layout.activity_scrolling_ponto);

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }


        try {
            //////////////////////////////// FIREBASE TOKEN ////////////////////////////////
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    // Gera token do firebase
                    TOKEN_FIREBASE = task.getResult();
                    // Atualiza Token do firebase em segundo plano para aliviar a main thread
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.err.println("TokenFirebase======> " + TOKEN_FIREBASE);
                                usuarioDAO.setTokenFirebasePushUsuario(TOKEN_FIREBASE);
                            } catch (Exception e) {
//                                AppLogErroDAO.gravaErroLOGServidor(usuario.getTipoCliente(), new Object() {
//                                }.getClass().getEnclosingMethod().getName() + " ERRO MSG: " + e.getMessage() + " STACKTRACE: " + e.getStackTrace().toString(), usuario.getCodigo(), Ferramentas.getMarcaModeloDispositivo(Splash.ctx));
                            }
                        }
                    }, 0);
                }
            });

            //////////////////////////////////////////////////////////////////////////////
        } catch (Exception e) {

        }


        try {
            if (Build.VERSION.SDK_INT >= 23) {
                String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions((Activity) EmpresaActivity.this, PERMISSIONS, 2);
            }
            mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
            dumpProviders();
            Criteria criteria = new Criteria();
            best = mgr.getBestProvider(criteria, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            Location location = mgr.getLastKnownLocation(best);
            dumpLocation(location);


            System.err.println("GPS ===> " + location.getLatitude() + ", " + location.getLongitude());

            solicitaGeoLocalizacao();
        } catch (Exception e) {
            System.err.println(e);
        }

        // Contect global
        EmpresaActivity.CTX = EmpresaActivity.this;
        // Animação inicial de abertura
        Animatoo.animateSwipeRight(this);

        // Mapa da geolocalização do ponto
        FloatingActionButton buttonLocalizacaoPonto = (FloatingActionButton) findViewById(R.id.buttonLocalizacaoPonto);
        buttonLocalizacaoPonto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentGeoLocalizacaoPonto = new Intent(EmpresaActivity.this, MapPontoActivity.class);
                startActivity(intentGeoLocalizacaoPonto);
            }
        });

        final View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        ImageView imageViewAspereza = (ImageView) bottomSheet.findViewById(R.id.imageViewAspereza);
        imageViewAspereza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExibeRoletaProdutosPonto();
            }
        });
        bottomSheet.requestLayout();
        bottomSheet.invalidate();

        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Duração da animação
                int duration = 1; // 100

                if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    // Exibe recyclerview dos grupos de produtos pra dar leveza na tela
                    final RecyclerView recyclerViewBotoesMenuPonto = (RecyclerView) findViewById(R.id.recyclerViewOpcoesPonto);
                    recyclerViewBotoesMenuPonto.animate()
                            .alpha(1f)
                            .setDuration(duration)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    recyclerViewBotoesMenuPonto.setVisibility(View.VISIBLE);
                                }
                            });
                } else {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        // Exibe recyclerview dos grupos de produtos pra dar leveza na tela
                        final RecyclerView recyclerViewBotoesMenuPonto = (RecyclerView) findViewById(R.id.recyclerViewOpcoesPonto);
                        recyclerViewBotoesMenuPonto.animate()
                                .alpha(0f)
                                .setDuration(duration)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        recyclerViewBotoesMenuPonto.setVisibility(View.GONE);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //
            }
        });


        bottomSheet.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return true;
            }
        });

        try {
            ///////////////////////////////////////////////////////////////
            //               CARREGA RECYCLERVIEWS PRINCIPAIS            //
            ///////////////////////////////////////////////////////////////
            // Carrega todos os grupos de produtos
            new AsyncTaskGetGruposEmpresa().execute();
            // Gera todas as recyclerviews
            setCarregaRecyclerViewRoletaBotoesMenuPrincipal();
            ///////////////////////////////////////////////////////////////
            // Set imagem e título da Empresa
            CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            TextView textViewNomeEmpresa = (TextView) findViewById(R.id.textViewNomeEmpresa);
            TextView textViewNomeEnderecoEmpresa = (TextView) findViewById(R.id.textViewNomeEnderecoEmpresa);
            toolbarLayout.setBackground(new BitmapDrawable(getResources(), ENTIDADE_EMPRESA.getImagem()));
            textViewNomeEmpresa.setText(ENTIDADE_EMPRESA.getNome());// Set nome
            textViewNomeEnderecoEmpresa.setText(ENTIDADE_EMPRESA.getEndereco()); // Set endereço
            // Animações
//            Ferramentas.setSombraTextView(textViewNomeEmpresa, 0, 0, VariaveisGlobais.ESPESSURA_BRILHO_OU_SOMBRA_TEXTOS, Color.BLACK);
//            Ferramentas.setSombraTextView(textViewNomeEnderecoEmpresa, 0, 0, VariaveisGlobais.ESPESSURA_BRILHO_OU_SOMBRA_TEXTOS, Color.BLACK);
        } catch (Exception e) {
            System.out.println("================> " + e);
            e.getStackTrace();
        }


        ///////////////////////////////////
        // Botão de voltar superior
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {

        super.onResume();
        try {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mgr.requestLocationUpdates(best, 15000, 1, (LocationListener) EmpresaActivity.this);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Carrega todas as recyclerviews da tela
     *
     * @author Igor Maximo
     * @date 26/04/2020
     */
    public void setCarregaRecyclerViewRoletaBotoesMenuPrincipal() {
        // Carrega RecyclerView com os botões de menu do ponto
        setRecyclerView((RecyclerView) findViewById(R.id.recyclerViewBotoesMenuPonto), 0, 0, new BotoesMenuPontoAdapter(getListaBotoesMenu()), 1, true, LinearLayoutManager.HORIZONTAL, false); // 0 para principal
    }

    /**
     * Carrega todas as recyclerviews da tela
     *
     * @author Igor Maximo
     * @date 26/04/2020
     */
    public void setCarregaRecyclerViewComanda() {
        // Carrega RecyclerView com as opções da empresa
        setRecyclerView((RecyclerView) findViewById(R.id.recyclerViewOpcoesPonto), 2, 1, new ComandaAdapter(), 1, false, LinearLayoutManager.VERTICAL, false);
    }

//// ??????????????????????????????????????????????????????????????????????????????????????????
//// ??????????????????????????????????????????????????????????????????????????????????????????
//// ??????????????????????????????????????????????????????????????????????????????????????????
//// ??????????????????????????????????????????????????????????????????????????????????????????
//// ??????????????????????????????????????????????????????????????????????????????????????????

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("MyString", "Welcome back to Android");
        savedInstanceState.setClassLoader(Carrinho.ENTIDADE_CARRINHO_PONTO);
        Carrinho.ENTIDADE_CARRINHO_PONTO = (EntidadeCarrinhoPonto) savedInstanceState.getClassLoader();
    }

    private long mLastKeyDownTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long current = System.currentTimeMillis();
        boolean res = false;
        if (current - mLastKeyDownTime < 300) {
            res = true;
        } else {
            res = super.onKeyDown(keyCode, event);
            mLastKeyDownTime = current;
        }
        return res;
    }


    /**
     * Gerador Principal de ReyclerView
     *
     * @param recyclerView
     * @param funcao
     * @param recyclerviewAdapter
     * @param qtdColunasReyclerView
     * @param seUsaOrientacaoLayout
     * @param orientacao
     * @usage Roleta de Botões do Menu
     * @usage Lista de Grupos de Produtos
     * @usage Roleta dos Produtos de um determinado grupo
     * @usage Lista dos itens no carrinho (COMANDA)
     * @author Igor Maximo
     * @date 26/04/2020
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setRecyclerView(final RecyclerView recyclerView, final int recyclerId, final int funcao, RecyclerView.Adapter recyclerviewAdapter, int qtdColunasReyclerView, boolean seUsaOrientacaoLayout, int orientacao, final boolean seUsaDoisParametros) {
        GruposEmpresaAdapter.FUNCAO = funcao;
        adapter = recyclerviewAdapter;
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new GridLayoutManager(this, qtdColunasReyclerView);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, qtdColunasReyclerView));
        if (seUsaOrientacaoLayout) {
            LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(this, orientacao, false);
            recyclerView.setLayoutManager(horizontalLayoutManagaer);
        }
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(true);

//        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
//        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

//        recyclerView.setFocusable(true);
        recyclerView.setNestedScrollingEnabled(false);

        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
        recyclerView.setNestedScrollingEnabled(false);


//        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                mLayoutManager.smoothScrollToPosition(recyclerView, null, adapter.getItemCount());
//            }
//        });


        // Para fazer o efeitos da bolinhas que indicam o index da recyclerview
//        if (funcao != 0) {
//            recyclerView.addItemDecoration(new IndicadoresIndexRecyclerView());
//        }

//        // Animação de centralizar o card durante scroll
//        LinearSnapHelper snapHelper = new SnapHelperOneByOne();
//        snapHelper.attachToRecyclerView(recyclerView);
//        new LinearSmoothScroller(recyclerView.getContext()) {
//            @Override
//            protected int getVerticalSnapPreference() {
//                return LinearSmoothScroller.SNAP_TO_ANY;
//            }
//
//            @Override
//            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
//                return 120f / displayMetrics.densityDpi;
//            }
//        };


//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(2);
//                    recyclerView.removeOnScrollListener(this);
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//
//            }
//        });


        // ON TOUCH
//        recyclerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Toast.makeText(EmpresaActivity.this, "Last", Toast.LENGTH_LONG).show();
//                return false;
//            }
//        });


        // ON SCROLL
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//                if (!recyclerView.canScrollVertically(1)) {
//                    Toast.makeText(EmpresaActivity.this, "Last", Toast.LENGTH_LONG).show();
//
//                }
//            }
//        });


        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(EmpresaActivity.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        System.out.println("pos ====> " + position);
                        System.out.println("recyclerId ====> " + recyclerId);
                        System.out.println("funcao ====> " + funcao);
                        System.out.println("botaoRoleta ====> " + botaoRoleta);

                        POSITION_ROLETA_BOTOES = position;
                        System.out.println("POSITION_ROLETA_BOTOES ====> " + POSITION_ROLETA_BOTOES);
                        switch (recyclerId) {
                            case 0: // Roleta botões menu
                                switch (position) { // Qual posição
//                                    case 0:
//                                        System.out.println(" ====> CAIU AQUI 0");
//                                        setDescartarCarrinho(EmpresaActivity.this);
//                                        break;
                                    case 0:
                                        // Seta esconde valor total da soma de todos os itens do carrinho
                                        CircularRevealGridLayout circularTotalConta = (CircularRevealGridLayout) findViewById(R.id.circularTotalConta);
                                        circularTotalConta.setVisibility(View.GONE);

                                        System.out.println(" ====> CAIU AQUI 1");
                                        Ferramentas.setRemoveAnimation((ImageView) findViewById(R.id.imageViewAspereza));
                                        botaoRoleta = 1;
                                        if (botaoRoleta == 1) {
                                            ImageView imageViewFotoRepresentacao = (ImageView) findViewById(R.id.imageViewFotoRepresentacao);
                                            imageViewFotoRepresentacao.setVisibility(View.GONE);

                                            setAlteraNomeBottomSheet("Cardápio", R.drawable.lanches, false);

                                            new AsyncTaskGetGruposEmpresa().execute();
                                            new AsyncTaskGetProdutosGrupoEscolhido().execute();
                                        }

                                        break;
                                    case 1:
                                        System.out.println(" ====> CAIU AQUI 2");
                                        Ferramentas.setHeartPulseAnimation((ImageView) findViewById(R.id.imageViewAspereza));
                                        ImageView imageViewFotoRepresentacao = (ImageView) findViewById(R.id.imageViewFotoRepresentacao);

                                        if (POSITION_ROLETA_BOTOES == 2) {
                                            imageViewFotoRepresentacao.setVisibility(View.VISIBLE);
                                            imageViewFotoRepresentacao.setBackgroundResource(R.drawable.formas_pagamento);
                                        } else {
                                            imageViewFotoRepresentacao.setVisibility(View.GONE);
                                        }

                                        botaoRoleta = 2;
                                        // Exibe valor total da conta no bottomsheet
                                        setAlteraNomeBottomSheet("Concluir Pedido", R.drawable.formas_pagamento, true);

                                        // Seta valor total da soma de todos os itens do carrinho
                                        circularTotalConta = (CircularRevealGridLayout) findViewById(R.id.circularTotalConta);
                                        circularTotalConta.setVisibility(View.VISIBLE);


                                        if (pos != 2) {
//                                            setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_EXPANDED);
                                        }
                                        // Variável recyclerViewProdutosGrupo em uso para o Cardápio
                                        setRecyclerView((RecyclerView) findViewById(R.id.recyclerViewOpcoesPonto), recyclerId, 1, new ComandaAdapter(), 1, false, LinearLayoutManager.VERTICAL, false);
                                        // Variável recyclerViewProdutosGrupo em uso para o Carrinho
                                        setRecyclerView((RecyclerView) findViewById(R.id.recyclerViewProdutosGrupo), recyclerId, 2, new EscolhaFormasPagamentoAdapter(getListaEtapasPagamento()), 1, true, LinearLayoutManager.HORIZONTAL, true);
                                        //setReyclerView((RecyclerView) findViewById(R.id.recyclerViewProdutosGrupo), recyclerId, 2, new EtapasPagamentoAdapter(getListaEtapasPagamento(), getListaEscolhaBandeira(), getListaDadosCartao()), 1, true, LinearLayoutManager.HORIZONTAL, true);
                                        break;
                                }
                                break;
                            case 1: // Lista grupos de produtos
                                switch (funcao) { // Qual posição
                                    case 0:
                                        if (botaoRoleta != 2) {
                                            EmpresaActivity.FK_GRUPO_PRODUTO_ESCOLHIDO = listaEntidadeGrupoProdutosEmpresa.get(position).getId();
//                                        // Esconde recyclerview dos grupos de produtos pra dar leveza na tela
//                                        RecyclerView recyclerViewBotoesMenuPonto = (RecyclerView) findViewById(R.id.recyclerViewOpcoesPonto);
//                                        recyclerViewBotoesMenuPonto.setVisibility(View.GONE);
                                            System.out.println(" ====> CAIU AQUI 3");
                                            Ferramentas.setRemoveAnimation((ImageView) findViewById(R.id.imageViewAspereza));
                                            // if (POSITION_ROLETA_BOTOES != )
                                            if ((pos != position || pos == -1)) {
                                                //if ((pos != position || pos == -1) && (position != 2 && recyclerId != 1)) {
                                                setAlteraNomeBottomSheet(listaEntidadeGrupoProdutosEmpresa.get(position).getNomeGrupo(), listaEntidadeGrupoProdutosEmpresa.get(position).getImagem(), false);
//                                            new AsyncTaskGetProdutosGrupoEscolhido().execute();
//                                            List<EntidadeProdutosGrupoEscolhido> lista = getListaGruposProdutosPonto();
                                                //   setReyclerView((RecyclerView) findViewById(R.id.recyclerViewProdutosGrupo), 2, 0, new ProdutoGrupoEscolhidoAdapter(listaEntidadeProdutosGrupoEscolhido), 1, true, LinearLayoutManager.HORIZONTAL, false);
                                                setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_EXPANDED);
                                            } else {
                                                setExibeRoletaProdutosPonto();
                                            }

                                            new AsyncTaskGetProdutosGrupoEscolhido().execute();
                                        }
                                        pos = position;

                                        break;
                                    case 1:
                                        System.out.println(" ====> CAIU AQUI 4");
                                        break;
                                    case 2:
                                        System.out.println(" ====> CAIU AQUI 5");
                                        break;

                                }
                                break;
                            case 2: // Lista produtos do grupo escolhido
                                switch (funcao) { // Qual posição
                                    case 0:
                                        System.out.println(" ====> CAIU AQUI 6");
                                        System.out.println("aqui");


                                        switch (botaoRoleta) {
                                            case 0:

                                                break;
                                            case 1: // Para adicionar os produtos no carrinho

                                                break;
                                            case 2: // Para abrir apenas se for no carrinho
                                                setGeraDialogPagarCartaoCreditoDebito(1, position);
                                                break;

                                        }

                                        break;
                                    case 1:

                                        break;
                                    case 2:

                                        break;

                                }
                                break;
                            case 3: // Lista itens do carrinho (comanda)
                                break;
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        //
                    }
                })
        );


        final RecyclerView recyclerViewAddOnPreDrawListener = recyclerView;
        recyclerViewAddOnPreDrawListener.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerViewAddOnPreDrawListener.getViewTreeObserver().removeOnPreDrawListener(this);

                for (int i = 0; i < recyclerViewAddOnPreDrawListener.getChildCount(); i++) {
                    View v = recyclerViewAddOnPreDrawListener.getChildAt(i);
                    v.setAlpha(0.0f);
                    v.animate().alpha(1.5f)
                            .setDuration(1000)
                            .setStartDelay(i * 150)
                            .start();
                }

                return true;
            }
        });

        recyclerviewAdapter.notifyDataSetChanged();
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

    /**
     * Ponte de escuta para as consultas de geolocalização
     *
     * @author Igor Maximo
     * @date 17/10/2019
     * @updated 17/10/2019
     */
    public Object[] solicitaGeoLocalizacao() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions((Activity) EmpresaActivity.this, PERMISSIONS, 2);
        }
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        dumpProviders();
        Criteria criteria = new Criteria();
        best = mgr.getBestProvider(criteria, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
        }
        Location location = mgr.getLastKnownLocation(best);
        dumpLocation(location);

        System.err.println("GPS => " + location.getLatitude() + ", " + location.getLongitude());

        return new Object[]{location.getLatitude(), location.getLongitude()};
    }

    private void dumpProviders(String s) {

        LocationProvider info = mgr.getProvider(s);
        StringBuilder builder = new StringBuilder();
        builder.append("name: ").append(info.getName());
    }

    private void dumpProviders() {
        List<String> providers = mgr.getAllProviders();
        for (String p : providers) {
            dumpProviders(p);
        }
    }

    private void dumpLocation(Location l) {
        if (l == null) {
            myLocationLatitude = 0.0;
            myLocationLongitude = 0.0;
        } else {
            myLocationLatitude = l.getLatitude();
            myLocationLongitude = l.getLongitude();
        }
    }

    /**
     * Exibe modal de dados do cartão
     *
     * @author Igor Maximo
     * @date 05/05/2020
     */
    private void setGeraDialogPagarCartaoCreditoDebito(int modoPagamento, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("MODO CRÉDITO");
        builder.setIcon(R.drawable.ic_cielo_png);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("WrongViewCast") final View view = inflater.inflate(R.layout.popup_pagar_cartao, null);


        TextView textViewTituloPOPUP = (TextView) view.findViewById(R.id.textViewTituloPOPUP);
        Ferramentas.setSombraTextView(textViewTituloPOPUP, 0, 0, 10.0f, Color.LTGRAY);

        LinearLayout linearBotaoConfirmarPagamento = (LinearLayout) view.findViewById(R.id.linearBotaoConfirmarPagamento);
        linearBotaoConfirmarPagamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTaskPagamento().execute();
            }
        });

        builder.setView(view);
        final AlertDialog show = builder.show();
    }

    /**
     * Exibe modal de detalhes do Produto
     *
     * @author Igor Maximo
     * @date 08/06/2020
     */
    public void setGeraDialogDetalhesProdutoEscolhido(Context context, String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CardInflateTheme);
//        builder.setTitle("Descrição:");
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.card_detalhe_produto, null);
        TextView textViewDetalheProdutoDescricao = (TextView) view.findViewById(R.id.textViewDetalheProdutoDescricao);
        if (msg == null || msg.length() == 0) {
            textViewDetalheProdutoDescricao.setText("Produto sem descrição...");
        } else {
            textViewDetalheProdutoDescricao.setText(msg);
        }
        builder.setView(view);
        builder.show();
    }

    /**
     * Exibe o "modal" para mostrar uma mensagem de aviso qualquer
     *
     * @author Igor Maximo
     * @data 18/04/2019
     */
    public void geraPopUpAlertaMsg(String msg, Context context, int layoutToastMsg) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(layoutToastMsg, (ViewGroup) ((Activity) context).findViewById(R.id.toast_layout_root_autorizada));
        TextView text = (TextView) layout.findViewById(R.id.textMsg);
        text.setText(msg);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
        // Minimiza o bottomsheet
//        this.setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Exibe o valor total somado carrinho
     *
     * @author Igor Maximo
     * @data 15/06/2020
     */
    @SuppressLint("SetTextI18n")
    public void setValoresComanda() {
        // Declaração dos campos
        TextView textViewTaxaEntrega = (TextView) findViewById(R.id.textViewTaxaEntrega);
        TextView textViewDesconto = (TextView) findViewById(R.id.textViewDesconto);
        TextView textViewValorTotalCarrinho = (TextView) findViewById(R.id.textViewValorTotalCarrinho);
        TextView textViewLabelTaxaEntrega = (TextView) findViewById(R.id.textViewLabelTaxaEntrega);
        TextView textViewLabelDesconto = (TextView) findViewById(R.id.textViewLabelDesconto);
        TextView textViewLabelTotalCarrinho = (TextView) findViewById(R.id.textViewLabelTotalCarrinho);
        // Set valores
        textViewValorTotalCarrinho.setText("R$ " + Ferramentas.setArredondaValorMoedaReal(AtributoCarrinho.VALOR_TOTAL_CARRINHO + ""));
        // Animações de sombreamento
        Ferramentas.setSombraTextView(textViewTaxaEntrega, 0, 0, 10.5f, Color.GRAY);
        Ferramentas.setSombraTextView(textViewDesconto, 0, 0, 10.5f, Color.GRAY);
        Ferramentas.setSombraTextView(textViewValorTotalCarrinho, 0, 0, 10.5f, Color.GRAY);
        Ferramentas.setSombraTextView(textViewLabelTaxaEntrega, 0, 0, 10.5f, Color.GRAY);
        Ferramentas.setSombraTextView(textViewLabelDesconto, 0, 0, 10.5f, Color.GRAY);
        Ferramentas.setSombraTextView(textViewLabelTotalCarrinho, 0, 0, 10.5f, Color.GRAY);
    }

    private int botaoRoleta = 1;
    private int pos = -1;

    /**
     * Altera o nome do BottomSheet para modalidade
     *
     * @author Igor Maximo
     * @date 29/04/2020
     */
    @SuppressLint("ResourceAsColor")
    private void setAlteraNomeBottomSheet(String tituloBottomSheet, Object drawableImageViewFotoGrupoBottomSheet, final boolean bottomSheetModosPagamento) {
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        TextView textViewNomeBottomSheet = bottomSheet.findViewById(R.id.textViewNomeBottomSheet);
        ImageView frisoPulsante = (ImageView) bottomSheet.findViewById(R.id.imageViewAspereza);
        // Muda cor se for escolhido os modos de pagamento
        if (bottomSheetModosPagamento) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            textViewNomeBottomSheet.setBackgroundColor(0xff228B22);

            Ferramentas.setFastHeartPulseAnimation(textViewNomeBottomSheet, EmpresaActivity.this);

            frisoPulsante.setVisibility(View.GONE);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING && bottomSheetModosPagamento) {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });

            // Modal modos de pagamento
            textViewNomeBottomSheet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Exibe modal
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EmpresaActivity.this);
                    alertDialogBuilder.setMessage("Deseja finalizar o pedido? ")
                            .setCancelable(false)
                            .setPositiveButton("Concluir",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Finaliza o pedido e envia os dados para o servidor
                                            new AsyncTaskFinalizarPedido().execute();
                                        }
                                    });
                    alertDialogBuilder.setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                }
            });
        } else {
            frisoPulsante.setVisibility(View.VISIBLE);
            textViewNomeBottomSheet.setBackgroundColor(0xfffbb000);
        }

        // Texto bottom sheet
        textViewNomeBottomSheet.setText(tituloBottomSheet);
    }

    private void setFinalizarPedido() {
        // Finaliza o pedido e envia os dados para o servidor
        new AsyncTaskFinalizarPedido().execute();
    }

    public List getListaEtapasPagamento() {

        List<EntidadePagamentoEscolhaModo> etapas = new ArrayList<>();

        // Escolha do método de pagamento
        EntidadePagamentoEscolhaModo entidadePagamentoEscolhaModo = new EntidadePagamentoEscolhaModo(
                "",
                "",
                "",
                EmpresaActivity.this
        );
        etapas.add(entidadePagamentoEscolhaModo);


        return etapas;
    }

    public List getListaEscolhaBandeira() {
        List<EntidadePagamentoBandeirasCartao> etapas = new ArrayList<>();

        // Se cartão de crédito
        EntidadePagamentoBandeirasCartao entidadeCartaoCredito = new EntidadePagamentoBandeirasCartao(
                "",
                0
        );
        etapas.add(entidadeCartaoCredito);

        return etapas;
    }

    public List getListaDadosCartao() {
        List<EntidadePagamentoCartaoCredito> etapas = new ArrayList<>();

        // Se cartão de crédito
        EntidadePagamentoCartaoCredito entidadeCartaoCredito = new EntidadePagamentoCartaoCredito(
                0,
                0f,
                "",
                EmpresaActivity.this
        );
        etapas.add(entidadeCartaoCredito);

        return etapas;
    }

    private BottomSheetBehavior mBottomSheetBehavior;


    public List getListaBotoesMenu() {

        List<EntidadeBotoesMenuPonto> botoesMenuPonto = new ArrayList<>();

//        EntidadeBotoesMenuPonto entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
//                0,
//                "Outros",
//                Integer.parseInt(String.valueOf(R.drawable.ic_outras_opcoes)),
//                null,
//                this
//        );
//        botoesMenuPonto.add(entidadeBotoesMenuPonto);

        EntidadeBotoesMenuPonto entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
                0,
                "Cardápio",
                Integer.parseInt(String.valueOf(R.drawable.ic_historico_pedidos)),
                null,
                this
        );
        botoesMenuPonto.add(entidadeBotoesMenuPonto);

        // Contém itens no carrinho
        if (AtributoCarrinho.ENTIDADES_ITENS_ESCOLHIDOS_DO_CARRINHO.size() != 0) {
            entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
                    0,
                    "Carrinho",
                    Integer.parseInt(String.valueOf(R.drawable.ic_carrinho)),
                    Integer.parseInt(String.valueOf(R.drawable.flag_carrinho_com_item)),
                    null,
                    this
            );
        } else {
            entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
                    0,
                    "Carrinho",
                    Integer.parseInt(String.valueOf(R.drawable.ic_carrinho)),
                    0,
                    null,
                    this
            );
        }

        botoesMenuPonto.add(entidadeBotoesMenuPonto);

//        entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
//                0,
//                "Promoções",
//                Integer.parseInt(String.valueOf(R.drawable.ic_promocoes)),
//                null,
//                this
//        );
//        botoesMenuPonto.add(entidadeBotoesMenuPonto);

        entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
                0,
                "Contato",
                Integer.parseInt(String.valueOf(R.drawable.ic_contato)),
                null,
                this
        );
        botoesMenuPonto.add(entidadeBotoesMenuPonto);

//        entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
//                0,
//                "Favoritos",
//                Integer.parseInt(String.valueOf(R.drawable.ic_favorito)),
//                null,
//                this
//        );
//        botoesMenuPonto.add(entidadeBotoesMenuPonto);

        entidadeBotoesMenuPonto = new EntidadeBotoesMenuPonto(
                0,
                "Histórico",
                Integer.parseInt(String.valueOf(R.drawable.ic_historico_desc)),
                null,
                this
        );
        botoesMenuPonto.add(entidadeBotoesMenuPonto);
        return botoesMenuPonto;
    }

    /**
     * @author Igor Maximo
     * @date 22/04/2020
     */
    public boolean setDescartarCarrinho(Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.CardInflateTheme);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_fechar_empresa, null);

        builder.setView(view);
        final Dialog dialogAddCarrinho = builder.create();

        LinearLayout buttonBotaoDescartarCarrinhoESair = (LinearLayout) view.findViewById(R.id.buttonBotaoDescartarCarrinhoESair);
        buttonBotaoDescartarCarrinhoESair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddCarrinho.dismiss();
                limpaCarrinho();
                onBackPressed();
            }
        });
        // Exibe o layout
        dialogAddCarrinho.show();
        return true;
    }

    @Override
    public void onBackPressed() {
        limpaCarrinho();
        super.onBackPressed();
    }

    private void limpaCarrinho() {
        // Limpa o carrinho
        AtributoCarrinho.ENTIDADES_ITENS_ESCOLHIDOS_DO_CARRINHO = new ArrayList<EntidadeProdutosGrupoEscolhido>(); // carrinho
        // Zera cálculos
        AtributoCarrinho.VALOR_TOTAL_CARRINHO = 0;
        AtributoCarrinho.VALOR_TOTAL_CREDITO = 0;
        AtributoCarrinho.VALOR_TOTAL_DEBITO = 0;
        AtributoCarrinho.VALOR_TOTAL_RECEBER_LOCAL = 0;
    }

    /**
     * Returns true if the given Activity has hardware acceleration enabled
     * in its manifest, or in its foreground window.
     * <p>
     * TODO(husky): Remove when initialize() is refactored (see TODO there)
     * TODO(dtrainor) This is still used by other classes.  Make sure to pull some version of this
     * out before removing it.
     */
    public static boolean hasHardwareAcceleration(Activity activity) {
        // Has HW acceleration been enabled manually in the current window?
        Window window = activity.getWindow();
        if (window != null) {
            if ((window.getAttributes().flags
                    & WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) != 0) {
                return true;
            }
        }

        // Has HW acceleration been enabled in the manifest?
        try {
            ActivityInfo info = activity.getPackageManager().getActivityInfo(
                    activity.getComponentName(), 0);
            if ((info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
//            Log.e("Chrome", "getActivityInfo(self) should not fail");
        }

        return false;
    }

    /**
     * Altera o state da bottom sheet
     *
     * @author Igor Maximo
     * @date 01/05/2020
     */
    @SuppressLint("ResourceAsColor")
    public void setExpandeColapsaBottomSheet(int state) {
        // Cores
    /*    CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setBackgroundColor(R.color.colorPretaTransp);
        NestedScrollView nestedEmpresa = (NestedScrollView) findViewById(R.id.nestedEmpresa);
        nestedEmpresa.setBackgroundColor(R.color.colorPretaTransp);
*/
        // Bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(state);
        bottomSheet.requestLayout();
        bottomSheet.invalidate();
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
                bottomSheet = findViewById(R.id.bottom_sheet_completar_cadastro_endereco);
                break;

        }
        bottomSheet.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                setExpandeColapsaBottomSheet(BottomSheetBehavior.STATE_EXPANDED, 0);
                return false;
            }
        });
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(state);
        bottomSheet.requestLayout();
        bottomSheet.invalidate();
    }

    /**
     * Exibe a roleta de produtos
     */
    public void setExibeRoletaProdutosPonto() {
        try {
            View bottomSheet = findViewById(R.id.bottom_sheet);
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


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    /**
     * AsyncTask para processamento paralelo do pagamento e spinner para congelar a tela
     *
     * @author Igor Maximo
     * @data 18/04/2019
     */
    private class AsyncTaskPagamento extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog mProgress = null;

        @Override
        protected Boolean doInBackground(String... strings) {
            ThreadRunningOperation();
            int i = 0;
            while (i < 99) {

            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPreExecute() {
            this.mProgress = ProgressDialog.show(EmpresaActivity.this, null, "Processando...", true);
            this.mProgress.setIndeterminate(false);
            this.mProgress.setCancelable(false);
//            ProgressBar progressbar=(ProgressBar) findViewById(android.R.id.progress);
//            progressbar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#6c0000"), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgress.dismiss();
        }

        private void ThreadRunningOperation() {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }


    /**
     * Classe privada que realiza a chamada assíncrona
     * da lista dos grupos de produtos
     *
     * @author Igor Maximo
     * @date 04/06/2020
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskGetGruposEmpresa extends AsyncTask<String, Integer, List<EntidadeGrupoProdutosEmpresa>> {
        private ProgressDialog mProgress = null;

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected List<EntidadeGrupoProdutosEmpresa> doInBackground(String... strings) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return new GruposEmpresaDAO().getListaGruposEmpresa(FK_EMPRESA_SELECIONADA);
        }

        @Override
        protected void onPreExecute() {
            this.mProgress = ProgressDialog.show(EmpresaActivity.this, null, "Processando...", true);
            this.mProgress.setIndeterminate(false);
            this.mProgress.setCancelable(false);
        }

        @Override
        protected void onPostExecute(List<EntidadeGrupoProdutosEmpresa> result) {
            try {
                // Verifica se a lista é maior que 0
                if (result.size() <= 0) {
                    setGeraPopUpAlertaMsg("Não há grupos de produtos!", EmpresaActivity.this, R.layout.toast_msg_alerta, true);
                    mProgress.dismiss();
                    super.onPostExecute(result);
                    return;
                }

                // Carrega a variável da lista estática global para o uso de qualquer método
                listaEntidadeGrupoProdutosEmpresa = result;
                // Carrega recyclerview com todos os grupos de produtos
                setRecyclerView((RecyclerView) findViewById(R.id.recyclerViewOpcoesPonto), 1, 0, new GruposEmpresaAdapter(result), 2, false, LinearLayoutManager.VERTICAL, false); // 0 para principal


                mProgress.dismiss();
                super.onPostExecute(result);
            } catch (Exception e) {
                System.out.println("=============> " + e);
                System.out.println("=============> " + e.getMessage());
                e.printStackTrace();
            }
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
     * Classe privada que realiza a chamada assíncrona
     * da lista dos produtos do grupo selecionado pelo cliente
     *
     * @author Igor Maximo
     * @date 04/06/2020
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskGetProdutosGrupoEscolhido extends AsyncTask<String, Integer, List<EntidadeProdutosGrupoEscolhido>> {
        private ProgressDialog mProgress = null;

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected List<EntidadeProdutosGrupoEscolhido> doInBackground(String... strings) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Consulta dos produtos por empresa e grupo
            return new ProdutoDAO().getListaProdutosGruposEmpresa(FK_EMPRESA_SELECIONADA, FK_GRUPO_PRODUTO_ESCOLHIDO);
        }

        @Override
        protected void onPreExecute() {
            this.mProgress = ProgressDialog.show(EmpresaActivity.this, null, "Processando...", true, false);
            this.mProgress.setIndeterminate(false);
            this.mProgress.setCancelable(false);
        }

        @Override
        protected void onPostExecute(List<EntidadeProdutosGrupoEscolhido> result) {
            try {
                // Verifica se a lista é maior que 0
                if (result.size() <= 0) {
                    setGeraPopUpAlertaMsg("Não há produtos!", EmpresaActivity.this, R.layout.toast_msg_alerta, true);
                    mProgress.dismiss();
                    super.onPostExecute(result);
                    return;
                }
                // Carrega a variável da lista estática global para o uso de qualquer método
                listaEntidadeProdutosGrupoEscolhido = result;
                // Carrega recyclerview com todos os produtos
                setRecyclerView((RecyclerView) findViewById(R.id.recyclerViewProdutosGrupo), 2, 0, new ProdutoGrupoEscolhidoAdapter(result), 1, false, LinearLayoutManager.VERTICAL, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgress.dismiss();
            super.onPostExecute(result);
        }
    }

    /**
     * Classe privada que realiza a chamada assíncrona
     * da lista dos produtos do grupo selecionado pelo cliente
     *
     * @author Igor Maximo
     * @date 04/06/2020
     */
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskFinalizarPedido extends AsyncTask<Void, Integer, Object[]> {
        private ProgressDialog mProgress = null;

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgress.setProgress(progress[0]);
        }

        @Override
        protected Object[] doInBackground(Void... strings) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Consulta dos produtos por empresa e grupo
            return new Carrinho().setFinalizarPedidoCarrinho();
        }

        @Override
        protected void onPreExecute() {
            this.mProgress = ProgressDialog.show(EmpresaActivity.this, null, "Processando...", true, false);
            this.mProgress.setIndeterminate(false);
            this.mProgress.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Object[] result) {
            mProgress.dismiss();
            super.onPostExecute(result);
        }
    }
}