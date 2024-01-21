package com.mycompany.catchkiller;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.media.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.widget.*;
import java.util.*;


class Fotos
{
	private int recurso;
	//private boolean usado;
	private int status;//0-free, 1-used, 2-dead
	public String nombre;
	public Fotos(int r, String n)
	{
		recurso=r;
		//usado=false;
		nombre=n;
		status=0;
		
	}
	public int usar()
	{
		//usado=true;
		status=1;
		return recurso;
	}
	public void liberar()
	{
		//usado=false;
		status=0;
	}
	public void kill()
	{
		status=2;
	}
	public boolean enUso()
	{
		return (status==1);
	}
	public boolean esLibre()
	{
		return (status==0);
	}
}

class Jugador
{
	public String nombre;
	public String nickName;
	public int foto;
	//public ImageView img;
	public String tipo;//Killer, Detective, Victim
	public double puntuacion;
	
	public Jugador(String nick, int f)
	{
		nickName=nick;
		foto=f;
		//img=image;
	}
}

class MyAL implements Animation.AnimationListener{
	ImageView img;
	Jugador player;
	ImageView img1;
	Jugador player1;
	int k;
	int p;
	int k1;
	int p1;
	Jugador[][] lPlayers;
	ImageView[][] lImgs;
	Boolean last=false;
	String killer;
	Pareja killerPos;
	
	Pareja[] suspects;
	Caso laVictima;
	int elArma;
	int N=MainActivity.N;
	Animation victimBlink;
	ImageView redBlock;
	Context context;
	GridLayout grid;
	Resources res;
	public MyAL(ImageView im, Jugador pl,ImageView im1, Jugador pl1,int k, int k1,int p, int p1)
	{
		this.player=pl;
		this.img=im;
		this.player1=pl1;
		this.img1=im1;
		this.k=k;
		this.k1=k1;
		this.p=p;
		this.p1=p1;
	}
	
	public void addBlinkBlock(int x,int y)
	{
		//RedBlock for the victim
		float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, res.getDisplayMetrics());
		redBlock=new ImageView(context);
		
		
		redBlock.setBackgroundColor(Color.RED);
		GridLayout.LayoutParams param =new GridLayout.LayoutParams();
		param.height = (int)pixels;
		param.width = (int)pixels;
		//param.rightMargin = 7;
		//param.topMargin = 7;
		param.setGravity(Gravity.CENTER);
		param.columnSpec = GridLayout.spec(x);
		param.rowSpec = GridLayout.spec(y);

		redBlock.setLayoutParams(param);
		grid.addView(redBlock);
		redBlock.startAnimation(victimBlink);
	}
	
	void matar(Pareja[] target)
	{
		
		Pareja victima=target[(int)(Math.random()*target.length)];
		laVictima.elMuerto=lPlayers[victima.x][victima.y];
		laVictima.index=victima;
		laVictima.arma=elArma;
		
		Pareja[] s=getTargets(victima,elArma);
		for(int i=0;i<8;i++){
			suspects[i]=s[i];
		}
	}
	
	Pareja getKillerPos(String killer)
	{
		Pareja res=null.;
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++)
			{
				if(lPlayers[i][j].nickName.equals(killer))
				{
					res=new Pareja(i,j);
				}
			}
			return res;
	}
	
	int selectWeapon()
	{
		return 1+(int)(Math.random()*3.0);
	}
	
	Pareja[] getTargets(Pareja killer,int arma)
	{
		Pareja[] res=new Pareja[8];
		int k=0;
		switch (arma){ //Knife
			case 1:
				for(int i=-1;i<2;i++)
					for(int j=-1;j<2;j++)
					{
						if((i!=0)||(j!=0))
						{
							res[k]=new Pareja((killer.x+i+2*N)%N,(killer.y+j+2*N)%N);
							k++;
						}
					}
				break;
			case 2: // gun
				for(int i=0;i<N;i++)
					if(i!=killer.x)
					{
						res[k]=new Pareja(i,killer.y);
						k++;
					}
					for(int i=0;i<N;i++)
					if(i!=killer.y)
					{
						res[k]=new Pareja(killer.x,i);
						k++;
					}
				break;
			case 3: // poisson
				
				for(int i:new int[]{-1,1})
					for(int j:new int[]{-1,1})
					for(int p:new int[]{1,2})
					{
						int d=3-p;
						res[k]=new Pareja((killer.x+p*i+2*N)%N,(killer.y+d*j+2*N)%N);
						k++;
					}
		}
		return res;
	}
	
	@Override
	public void onAnimationStart(Animation animation) {
		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		img.setImageResource(player1.foto);
		img1.setImageResource(player.foto);
		MainActivity.swapPlayers(player, player1);
		if(last)
		{
			//Commit the murder
			elArma=selectWeapon();
			Pareja kl=getKillerPos(killer);
			killerPos.x=kl.x;
			killerPos.y=kl.y;
			matar(getTargets(kl,elArma));
			//victim blinking
			addBlinkBlock(laVictima.index.x,laVictima.index.y);
			//lImgs[laVictima.index.x][laVictima.index.y].startAnimation(victimBlink);
			//resaltar sospechosos
			for(int i=0;i<N;i++)
				for(int j=0;j<N;j++)
					lImgs[i][j].setAlpha(0.4f);
			for(Pareja s: suspects){
				lImgs[s.x][s.y].setAlpha(1.0f);
			}
			lImgs[laVictima.index.x][laVictima.index.y].setAlpha(1.0f);
			
			//sonido
			float volume = 1.0f;// whatever in the range = 0.0 to 1.0
			// play sound with same right and left volume, with a priority of 1, 
			// zero repeats (i.e play once), and a playback rate of 1f
			
			MainActivity.soundPool.play(MainActivity.soundPoolMap.get(elArma), volume, volume, 1, 0, 1f);
			MainActivity.nButton.setEnabled(true);
			MainActivity.enableCells();
			MainActivity.nGameVictims++;
			Toast.makeText(context, "Oh my God! "+laVictima.elMuerto.nickName+ " was "+Arsenal.tipo[elArma].action, Toast.LENGTH_LONG).show();
		}
	}
}
	
class Pareja
{
	public int x;
	public int y;
	public Pareja(int x,int y)
	{
		this.x=x;
		this.y=y;
	}
}

class Arma
{
	
	String nombre;
	String action;
	public Arma(String n,String a)
	{
		nombre=n;
		action=a;
	}
}

class Arsenal
{
	static Arma tipo[]={new Arma("",""),
						new Arma("knife", "stabbed"),
						new Arma("gun","shot"),
						new Arma("poisson","poissoned")};
}

class Caso
{
	public Jugador elMuerto;
	public Pareja index;
	public int arma;// indice en arsanal
	public Caso(Jugador m,Pareja id, int arma)
	{
		elMuerto=m;
		index=id;
		this.arma=arma;
	}
}

/*class Suspects
{
	Pareja[] lista=new Pareja[8];
}*/


public class MainActivity extends Activity 
{
	//private Handler mHandler;
	public TextView avText;
	public static int N=5;
	public Fotos[] listaFotos=new Fotos[60];
	public static Jugador[][] players=new Jugador[N][N];
	public String theKiller;
	public Pareja killerPos;
	public Pareja[] suspects=new Pareja[8];
	public Caso theCase;
	public int average;
	public int nTotalVictims=0;
	public static int nGameVictims=0;
	public int nArrestedKillers=0;
	GridLayout laGrid;
	public static ImageView[][] imgs=new ImageView[N][N];
	Animation blink;
	ImageView redBlock;
	public static SoundPool soundPool;
	public static HashMap soundPoolMap;
	boolean newGame=true;
	public static Button nButton;
	public static OnClickListener imgClick;
	static MediaPlayer mp;
	//SpringAnimation spring;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		avText=(TextView)findViewById(R.id.averageView);
		initSounds(this);
		laGrid=findViewById(R.id.mainGridLayout1);
		iniFotos();
		iniPlayers();
		theKiller=selectKiller();
		nButton=(Button)findViewById(R.id.nextButton);
		nButton.setText("New game");
		/** Play the sound using android.media.MediaPlayer */
		
		mp = MediaPlayer.create(this, R.raw.game);  
		mp.setLooping(true);
		mp.setVolume(0.3f,0.3f);
		mp.start();
		
		
		
		imgClick=new OnClickListener(){
			public void onClick(View view) {
				MediaPlayer mp;
				if(view.equals(imgs[killerPos.x][killerPos.y]))
				{
					nArrestedKillers+=1;
					Toast.makeText(getBaseContext(), "Congrats. You got the killer!", Toast.LENGTH_LONG).show();
					mp = MediaPlayer.create(getBaseContext(), R.raw.ok);  
					
				} else {
					Toast.makeText(getBaseContext(), "Sorry. You are wrong.The killer is "+theKiller+ ". You can play a new game to catch a new killer", Toast.LENGTH_LONG).show();
					mp = MediaPlayer.create(getBaseContext(), R.raw.nooo);
					newGame=true;
					if (nGameVictims<10) nGameVictims=10;
					showKiller();
				}
				mp.setVolume(0.3f,0.3f);
				mp.start();
				newGame=true;
				nTotalVictims+=nGameVictims;
				
				nGameVictims=0;
				nButton.setText("New Game");
				disableCells();
				calcularAverage();
			}
		};
		//enableCells();
		
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);

			mp.stop();
			mp.reset();
			mp.release();
			mp=null;
			finishAndRemoveTask();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public static void enableCells()
	{
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++){
				imgs[i][j].setOnClickListener(imgClick);
			}
	}
	
	public static void disableCells()
	{
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++){
				imgs[i][j].setOnClickListener(null);
			}
	}
	
	public void initSounds(Context context) {
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap(3);     
		soundPoolMap.put(1, soundPool.load(context, R.raw.knife, 1) );
		soundPoolMap.put(2, soundPool.load(context, R.raw.gun, 2) );
		soundPoolMap.put(3, soundPool.load(context, R.raw.poisson, 3) );
		//soundPoolMap.put(10, soundPool.load(context, R.raw.game, 4) );
	}
	
	public void replaceVictim()
	{
		try{
		int x=theCase.index.x;
		int y=theCase.index.y;
		for(Fotos f: listaFotos){
			if(f.esLibre()){
				players[x][y].foto=f.usar();
				players[x][y].nickName=f.nombre;
				imgs[x][y].setImageResource(players[x][y].foto);
				break;
			}
		}
		} catch(Exception e){}
	}
	
	public void nextOnClick(View view)
	{
		if (newGame){
			juegoNuevo();

			newGame=false;
			nButton.setText("Next");
		}else{
			//calcularAverage();
			replaceVictim();
		}	
		
		laGrid.removeAllViewsInLayout();
		
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++)
			{
				imgs[i][j].setVisibility(View.VISIBLE);
				imgs[i][j].setAlpha(1.0f);
				laGrid.addView(imgs[i][j],0);
			}
		regarPlayers(0);
		try {
	  	  blink.cancel();
		} catch(Exception e){};
		animatePlayers1(10);
		
		nButton.setEnabled(false);
	}
	
	int siguiente(int i,int n)
	{
		int res=i+1;
		if(res==n) res=0;
		return res;
	}
	
	void calcularAverage()
	{
		average=(int)(1000.0*(double)nArrestedKillers/(double)nTotalVictims);
		avText.setText(Integer.toString(average));
	}
	
	void animatePlayers1(int veces)
	{
		final ImageView[] aMover=new ImageView[veces];
		final ImageView[] hasta=new ImageView[veces];
		
		for(int i=0;i<veces;i++)
		{
		     int k=(int)(Math.random()*N);
			 int p=(int)(Math.random()*N);
			 int k1=(int)(Math.random()*N);
			 int p1=(int)(Math.random()*N);
			aMover[i]=imgs[k][p];
			hasta[i]=imgs[k1][p1];
			
			TranslateAnimation animation = new TranslateAnimation(0, hasta[i].getX()-aMover[i].getX(),0 , hasta[i].getY()-aMover[i].getY());
			animation.setRepeatMode(0);
			animation.setDuration(500);
			animation.setStartOffset(i*500);
			//Custom AnimationListener
			//se le pasan los parametros al ultomo de ellos
			//para ejecutar el murder al final del utimo swap
			MyAL al1=new MyAL(aMover[i],players[k][p],hasta[i],players[k1][p1],k,p,k1,p1);
			if(i==veces-1)
			{
				al1.last=true;
				al1.killer=theKiller;
				al1.lImgs=imgs;
				al1.lPlayers=players;
				theCase=new Caso(null,null,0);
				al1.laVictima=theCase;
				for(int j=0;j<8;j++)
				{
					suspects[j]=new Pareja(0,0);
				}
				al1.suspects=suspects;
				killerPos=new Pareja(1000,1000);
				al1.killerPos=killerPos;
			}
			
			//animation for victim
			blink= new AlphaAnimation(0.5f, 0); // Change alpha from fully visible to invisible
			blink.setDuration(300); // duration
			blink.setInterpolator(new LinearInterpolator()); // do not alter animation rate
			blink.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
			blink.setRepeatMode(Animation.REVERSE);
			al1.victimBlink=blink;
			al1.res=getResources();
			al1.context=this;
			al1.grid=laGrid;
			
			animation.setAnimationListener(al1);
			aMover[i].bringToFront();
			aMover[i].startAnimation(animation);
			
		}
	}
	
	
	
	public String selectKiller()
	{
		int k=(int)(Math.random()*N);
		int p=(int)(Math.random()*N);
		return 	players[k][p].nickName;
	}
	
	void regarPlayers(int veces)
	{
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++)
			{
				int k=(int)(Math.random()*(double)N);
				int p=(int)(Math.random()*(double)N);
				
				Jugador auxi=players[i][j];
				players[i][j]=players[k][p];
				players[k][p]=auxi;
				
				imgs[i][j].setImageResource(players[i][j].foto);
				imgs[k][p].setImageResource(auxi.foto);
			}
	}
	
	public static void swapPlayers(Jugador pl1, Jugador pl2)
	{
		String nom=pl1.nombre;
		String nk=pl1.nickName;
		int f= pl1.foto;
		String t=pl1.tipo;
		double punt=pl1.puntuacion;
		
		pl1.nombre=pl2.nombre;
		pl1.nickName=pl2.nickName;
		pl1.foto=pl2.foto;
		pl1.tipo=pl2.tipo;
		pl1.puntuacion=pl2.puntuacion;
		
		pl2.nombre=nom;
		pl2.nickName=nk;
		pl2.foto=f;
		pl2.tipo=t;
		pl2.puntuacion=punt;
	}
	
	void iniPlayers()
	{
		//float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 83, getResources().getDisplayMetrics());
		//Calculando longitud del lado de la ficha
		float pixels;
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		int screenWidth = size.x; //ancho de la pantalla
		pixels=(float)screenWidth/5.0f;
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++)
			{
				int index=i*N+j;
				imgs[i][j]=new ImageView(this);
				players[i][j]=new Jugador(listaFotos[index].nombre,listaFotos[index].usar());
				imgs[i][j].setImageResource(players[i][j].foto);
				GridLayout.LayoutParams param =new GridLayout.LayoutParams();
				
				param.height = (int)pixels;
				param.width = (int)pixels;
				
				//param.rightMargin = 7;
				//param.topMargin = 7;
				param.setGravity(Gravity.CENTER);
				param.columnSpec = GridLayout.spec(i);
				param.rowSpec = GridLayout.spec(j);
				imgs[i][j].setLayoutParams(param);
				laGrid.addView(imgs[i][j]);
			}
	}
	
	void iniFotos()
	{
		listaFotos[0]=new Fotos(R.drawable.adriel,"Adriel");
		listaFotos[1]=new Fotos(R.drawable.ale, "Ale");
		listaFotos[2]=new Fotos(R.drawable.arjona, "Arjona");
		listaFotos[3]=new Fotos(R.drawable.arnold,"Arnold");
		listaFotos[4]=new Fotos(R.drawable.barbara, "Barbara");
		listaFotos[5]=new Fotos(R.drawable.psico,"Calviño");
		listaFotos[6]=new Fotos(R.drawable.shrek,"Shrek");
		listaFotos[7]=new Fotos(R.drawable.luism,"Luis");
		listaFotos[8]=new Fotos(R.drawable.mary1,"Mary");
		listaFotos[9]=new Fotos(R.drawable.bella,"Bella");
		listaFotos[10]=new Fotos(R.drawable.brad,"Brad");
		listaFotos[11]=new Fotos(R.drawable.caballero,"Caballero");
		listaFotos[12]=new Fotos(R.drawable.celine,"Celine");
		listaFotos[13]=new Fotos(R.drawable.chivo,"El Chivo");
		listaFotos[14]=new Fotos(R.drawable.cristina,"Cristina");
		listaFotos[16]=new Fotos(R.drawable.danel,"Danel");
		listaFotos[17]=new Fotos(R.drawable.davidb,"David");
		listaFotos[18]=new Fotos(R.drawable.dicaprio,"Leonardo");
		listaFotos[19]=new Fotos(R.drawable.doll,"Muñeca");
		listaFotos[20]=new Fotos(R.drawable.duran,"Durán");
		listaFotos[21]=new Fotos(R.drawable.einstein,"Einstein");
		listaFotos[22]=new Fotos(R.drawable.frida,"Frida");
		listaFotos[23]=new Fotos(R.drawable.gato,"El Gato");
		listaFotos[24]=new Fotos(R.drawable.gremlin,"El Gremlin");
		listaFotos[25]=new Fotos(R.drawable.harry,"Harry");
		listaFotos[26]=new Fotos(R.drawable.hugh,"Hugh");
		listaFotos[27]=new Fotos(R.drawable.ic_launcher,"El Robot");
		listaFotos[28]=new Fotos(R.drawable.idy,"Idy");
		listaFotos[29]=new Fotos(R.drawable.isra,"Isra");
		listaFotos[30]=new Fotos(R.drawable.jedi,"El Jedi");
		listaFotos[31]=new Fotos(R.drawable.jennifer,"Jenny");
		listaFotos[32]=new Fotos(R.drawable.jlopez,"JLo");
		listaFotos[33]=new Fotos(R.drawable.leon,"El León");
		listaFotos[34]=new Fotos(R.drawable.liz,"Liz");
		listaFotos[35]=new Fotos(R.drawable.loly,"Loly");
		listaFotos[36]=new Fotos(R.drawable.mcarey,"Mariah");
		listaFotos[37]=new Fotos(R.drawable.mdouglas,"Douglas");
		listaFotos[38]=new Fotos(R.drawable.meryl,"Meryl");
		listaFotos[39]=new Fotos(R.drawable.mima,"Silvia");
		listaFotos[40]=new Fotos(R.drawable.mjackson,"Michael");
		listaFotos[41]=new Fotos(R.drawable.mozart,"Mozart");
		listaFotos[42]=new Fotos(R.drawable.mulan,"Mulán");
		listaFotos[43]=new Fotos(R.drawable.newton,"Isaac");
		listaFotos[44]=new Fotos(R.drawable.papa,"Patiño");
		listaFotos[45]=new Fotos(R.drawable.paul,"Paul");
		listaFotos[46]=new Fotos(R.drawable.pelusin,"Pelusín");
		listaFotos[47]=new Fotos(R.drawable.perro,"El perro");
		listaFotos[48]=new Fotos(R.drawable.pugachov,"Emiliano");
		listaFotos[49]=new Fotos(R.drawable.raul,"Raúl");
		listaFotos[50]=new Fotos(R.drawable.rcarlos,"Roberto");
		listaFotos[51]=new Fotos(R.drawable.rosa,"Rosita");
		listaFotos[52]=new Fotos(R.drawable.rubiera,"Rubiera");
		listaFotos[53]=new Fotos(R.drawable.shakira,"Shakira");
		listaFotos[54]=new Fotos(R.drawable.sheldon,"Sheldon");
		listaFotos[55]=new Fotos(R.drawable.silvio,"Silvio");
		listaFotos[56]=new Fotos(R.drawable.sirenita,"La Sirenita");
		listaFotos[57]=new Fotos(R.drawable.suly,"Suly");
		listaFotos[58]=new Fotos(R.drawable.trump,"Donald");
		listaFotos[59]=new Fotos(R.drawable.vaca,"La Vaca");
		listaFotos[15]=new Fotos(R.drawable.yeya,"Yeya");
		regarFotos();
	}
	void regarFotos()
	{
		Fotos auxi;
		for (int i=0;i<listaFotos.length;i++)
		{
			int k=(int)(Math.random()*(double)listaFotos.length);
			auxi=listaFotos[i];
			listaFotos[i]=listaFotos[k];
			listaFotos[k]=auxi;
		}
	}
	
	void juegoNuevo()
	{
		regarFotos();
		for(Fotos f:listaFotos)
			f.liberar();
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++)
			{
				int index=i*N+j;
				
				players[i][j].nickName =listaFotos[index].nombre;
				players[i][j].foto=listaFotos[index].usar();
				imgs[i][j].setImageResource(players[i][j].foto);
			}	
		theKiller=selectKiller();
	}
	
	void showKiller()
	{
		Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom);
		
		for(int i=0;i<N;i++)
		for(int j=0;j<N;j++)
		{
			if(players[i][j].nickName.equals(theKiller)){
				AnimationSet aS=new AnimationSet(true);
				//imgs[i][j].startAnimation(aniSlide);
				//moveViewToScreenCenter(imgs[i][j]);
				aniSlide.setFillAfter(true);
				aniSlide.setFillBefore(true);
				aniSlide.setInterpolator(new DecelerateInterpolator());
				TranslateAnimation anim = new TranslateAnimation( 0, imgs[0][0].getX()- imgs[i][j].getX() , 0, imgs[0][0].getY() -  imgs[i][j].getY());
				anim.setDuration(1000);
				anim.setFillAfter( true );
				//aS.addAnimation(anim);
				aS.addAnimation(aniSlide);
				aS.addAnimation(anim);
				imgs[i][j].bringToFront();
				imgs[i][j].startAnimation(aS);
				
				break;
			}
		}
	}
	private void moveViewToScreenCenter( View view )
	{
		GridLayout root = laGrid;
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics( dm );
		int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();

		int originalPos[] = new int[2];
		view.getLocationOnScreen( originalPos );

		int xDest = dm.widthPixels/2;
		xDest -= (view.getMeasuredWidth()/2);
		int yDest = dm.heightPixels/2 - (view.getMeasuredHeight()/2) - statusBarOffset;

		TranslateAnimation anim = new TranslateAnimation( 0, xDest - originalPos[0] , 0, yDest - originalPos[1] );
		anim.setDuration(1000);
		anim.setFillAfter( true );
		view.startAnimation(anim);
	}
	
}
