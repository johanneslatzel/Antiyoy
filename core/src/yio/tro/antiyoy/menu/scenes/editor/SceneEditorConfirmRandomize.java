package yio.tro.antiyoy.menu.scenes.editor;

import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.behaviors.editor.EditorReactions;
import yio.tro.antiyoy.menu.scenes.AbstractScene;

public class SceneEditorConfirmRandomize extends AbstractScene {


    public SceneEditorConfirmRandomize(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);
    }


    @Override
    public void create() {
        menuControllerYio.hideAllEditorPanels();

        menuControllerYio.getYioGdxGame().beginBackgroundChange(3, true, true);

        createInvisibleButton();
        createBasePanel();
        createYesButton();
        createCancelButton();
    }


    private void createInvisibleButton() {
        ButtonYio invisibleButton = buttonFactory.getButton(generateRectangle(0, 0, 1, 1), 523, null);
        invisibleButton.setRenderable(false);
    }


    private void createCancelButton() {
        ButtonYio cancelButton = buttonFactory.getButton(generateRectangle(0.025, 0.15, 0.475, 0.06), 522, getString("cancel"));
        cancelButton.setReaction(EditorReactions.rbEditorHideConfirmRandomize);
        cancelButton.setShadow(false);
        cancelButton.setAnimation(Animation.FIXED_DOWN);
    }


    private void createYesButton() {
        ButtonYio yesButton = buttonFactory.getButton(generateRectangle(0.5, 0.15, 0.475, 0.06), 521, getString("yes"));
        yesButton.setReaction(EditorReactions.rbEditorRandomize);
        yesButton.setShadow(false);
        yesButton.setAnimation(Animation.FIXED_DOWN);
    }


    private void createBasePanel() {
        ButtonYio basePanel = buttonFactory.getButton(generateRectangle(0.025, 0.15, 0.95, 0.16), 520, null);
        if (basePanel.notRendered()) {
            basePanel.addTextLine(getString("confirm_randomize"));
            basePanel.addTextLine(" ");
            basePanel.addTextLine(" ");
            basePanel.addTextLine(" ");
            menuControllerYio.getButtonRenderer().renderButton(basePanel);
        }
        basePanel.setTouchable(false);
        basePanel.setAnimation(Animation.FIXED_DOWN);
    }


    public void hide() {
        destroyByIndex(520, 529);
    }
}
