package edu.uta.byos.Managers;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.ui.IGameInterface.OnCreateSceneCallback;

import android.util.SparseArray;

import edu.uta.byos.GameMenu;
import edu.uta.byos.ManagedScene;
import edu.uta.byos.TableauScene;
import edu.uta.byos.runtime.Anchor;
//import edu.uta.byos.MainMenuScene;
//import edu.uta.byos.SplashScene;
import edu.uta.byos.runtime.Card;
import edu.uta.byos.runtime.SpiderAnchor;


/**
* ******************************** [ ByOS ] ***********************************
* @Description A solitaire game
* @Class    | SceneManager
*           | Responsible for switching between scenes and keeping track of the current
*           | displayed scene.
*           | Design Pattern: SINGLETON
*           | Enum: Shows the scene type
* @authors ruby_
* @version 3.8
* @since v-2.0
* ******************************************************************************
*/

public class SceneManager extends Object {

    // -------------------------------
    // Constants
    // -------------------------------
    private static final SceneManager INSTANCE = new SceneManager();

    // -------------------------------
    // Constructors
    // -------------------------------
    private SceneManager() {
    }
 	
    // -------------------------------
    // Scenes
    // -------------------------------
    private ManagedScene splashScene;
    private GameMenu menuScene;
    private ManagedScene gameScene;
    private ManagedScene loadingScene;

//    private ManagedScene currentScene;
    private Scene currentScene;

    // -------------------------------
    // Fields
    // -------------------------------
    public enum SceneType {
        SCENE_SPLASH,
        SCENE_MENU,
        SCENE_GAME,
        SCENE_LOADING,
    }

    private SceneType currentSceneType = SceneType.SCENE_GAME;
    private Engine mEngine = ResourceManager.getInstance().engine;

    // -------------------------------
    // Getter & Setter
    // -------------------------------
    /* Singleton reference to the global SceneManager */
    public static SceneManager getInstance() {
        return INSTANCE;
    }

    public SceneType getCurrentSceneType() {
        return currentSceneType;
    }

//    public ManagedScene getCurrentScene() {
//        return currentScene;
//    }
    
    public Scene getCurrentScene() {
    	return currentScene;
    }

    // -------------------------------
    // public Methods
    // -------------------------------
    /* Set scene */
    public void setScene(ManagedScene pScene) {
        mEngine.setScene(pScene);
        currentScene = pScene;
        currentSceneType = pScene.getSceneType();
    }

    /* Switch scene */
    public void setScene(SceneType pSceneType) {
        switch (pSceneType)
        {
            case SCENE_SPLASH:
                setScene(splashScene);
                break;
//            case SCENE_MENU:
//                setScene(menuScene);
//                break;
            case SCENE_GAME:
                setScene(gameScene);
                break;
            case SCENE_LOADING:
                setScene(loadingScene);
                break;
            default:
                break;
        }
    }

    // ============================ INIT SCENE (SPLASH) ================= //
    /* Initialize the splash scene */
//    public void onShowSplashScene(OnCreateSceneCallback pOnCreateSceneCallback) {
//        ResourceManager.loadSplashResources();
//        splashScene = new SplashScene();
//        currentScene = splashScene;
//        pOnCreateSceneCallback.onCreateSceneFinished(splashScene);
//    }

    /* Dispose splash scene -- after load the menu */
//    private void onDisposeSplashScene() {
//        ResourceManager.unloadSplashResources();
//        splashScene.onDisposeScene();
//        splashScene = null;
//    }

    // ============================ SCENE (MENU) ================= //
    /* Initialize the main menu */
    public void onShowMenuScene() {
        ResourceManager.loadMenuResources();
        
        menuScene = new GameMenu(ResourceManager.getInstance().engine.getCamera());
        menuScene.onPopulateMenu();
      
        getCurrentScene().setChildScene(menuScene, false, true, true);
        currentSceneType = SceneType.SCENE_MENU;
    }

    // ============================ SCENE (GAME) ================= //
    /* Initialize the game scene */
    public void onShowGameScene(OnCreateSceneCallback pOnCreateSceneCallback) {
    	/* Load Resources before createScene */
        ResourceManager.loadGameResources();
        
        /* Get cards ready */
        GameManager.getInstance().allCardTurnOff();
        GameManager.getInstance().initAllCards(4);
        GameManager.getInstance().initReadyCard();
        
        gameScene = new TableauScene();
        gameScene.sortChildren();
        
        /* The actual collision-checking. */
    	gameScene.registerUpdateHandler(new IUpdateHandler() {
    		
    		/* localAnchor is used to store which anchor the SELECTEDCARD is chosen from*/
    		SpiderAnchor originalAnchor = new SpiderAnchor();
    		
    		@Override
    		public void reset() { }

    		@Override
    		public void onUpdate(final float pSecondsElapsed) {
    			
    			/* Check if SELECTEDCARD can be moved to any anchor, if so, append the selected card to 
    			 * the anchor and turn on the last card from which anchor the card is selected
    			 */
    			
    			SpiderAnchor localAnchor = new SpiderAnchor();
    			for(int i = 0; i < GameManager.mAnchorList.size(); i++) {

    				localAnchor = (SpiderAnchor)GameManager.mAnchorList.get(i);
    				Card localCard = localAnchor.getLastCard();
    				
    				/* Remember the anchor that the SELECTEDCARD is from */
    				if(GameManager.SELECTEDCARD!=null && localCard == GameManager.SELECTEDCARD) {
    					originalAnchor = (SpiderAnchor)(GameManager.mAnchorList.get(i));
    					originalAnchor.removeCard(GameManager.SELECTEDCARD);
    				}
    				
    				/* Move card procedure - moving*/
    				if(GameManager.SELECTEDCARD != null && GameManager.SELECTEDCARD.isGrabbed()) {
    					if(localCard.collidesWith(GameManager.SELECTEDCARD)) 
    						localCard.clicked();
    					else
    						localCard.release();
    				}
    				
    				/* Move card procedure - end*/
    				if(GameManager.SELECTEDCARD != null && !GameManager.SELECTEDCARD.isGrabbed()) {
    					if(localCard.isClicked()) {
    						if(localAnchor.isCanAppendCard(GameManager.SELECTEDCARD)) {
    							localAnchor.appendCard(GameManager.SELECTEDCARD);
    							
    							originalAnchor.getLastCard().onTurnOn();
    							getCurrentScene().registerTouchArea(originalAnchor.getLastCard());

    						} else {
    							originalAnchor.appendCard(GameManager.SELECTEDCARD);
    						}

    						localCard.release();
    						GameManager.SELECTEDCARD = null;
    					}
    				}
    				
    				
    			} //end of for-loop
    		
    			/* Moved card is not within touchable area*/
//    			if(GameManager.SELECTEDCARD != null && !GameManager.SELECTEDCARD.isGrabbed()) {
//    				boolean inTouchedArea = false;
//    				for(int i = 0; i < GameManager.mAnchorList.size(); i++) {
//    					if(GameManager.mAnchorList.get(i).getLastCard().isClicked())
//    						inTouchedArea = true;
//    				}
//    				
//					if(inTouchedArea == true)
//						GameManager.SELECTEDCARD.onTurnOff();
//					else
//						GameManager.SELECTEDCARD.onTurnOn();
//				}
    			
    		}//end of onUpdate Override
    		
    	});
        setScene(gameScene);
        pOnCreateSceneCallback.onCreateSceneFinished(gameScene);
    }
    
    public void onShowGameScene() {
    	menuScene.back();
    	currentScene = gameScene;
    	currentSceneType = SceneType.SCENE_GAME;
    }

}
