package yio.tro.antiyoy.menu.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.menu.InterfaceElement;
import yio.tro.antiyoy.menu.fast_construction.FastConstructionPanel;
import yio.tro.antiyoy.menu.fast_construction.FcpItem;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.RectangleYio;

public class RenderFastConstructionPanel extends MenuRender{


    private TextureRegion backgroundTexture;
    private TextureRegion selectionPixel;
    private FastConstructionPanel panel;
    private float factor;
    private RectangleYio pos;
    private TextureRegion man0;
    private TextureRegion man1;
    private TextureRegion man2;
    private TextureRegion man3;
    private TextureRegion tower;
    private TextureRegion strongTower;
    private TextureRegion house;
    private TextureRegion sideShadow;
    private float smDelta;
    private TextureRegion endTurnIcon;
    private TextureRegion undoIcon;
    private TextureRegion diplomacyIcon;
    private TextureRegion diplomacyRedIcon;


    @Override
    public void loadTextures() {
        backgroundTexture = GraphicsYio.loadTextureRegion("pixels/gray_pixel.png", false);
        selectionPixel = GraphicsYio.loadTextureRegion("pixels/black_pixel.png", false);
        sideShadow = GraphicsYio.loadTextureRegion("money_shadow.png", true);
        endTurnIcon = GraphicsYio.loadTextureRegion("end_turn.png", true);
        undoIcon = GraphicsYio.loadTextureRegion("undo.png", true);
        diplomacyIcon = GraphicsYio.loadTextureRegion("diplomacy/flag.png", true);
        diplomacyRedIcon = GraphicsYio.loadTextureRegion("diplomacy/flag_red.png", true);

        loadSkinDependentTextures();
    }


    private void loadSkinDependentTextures() {
        man0 = loadFromFieldElements("man0");
        man1 = loadFromFieldElements("man1");
        man2 = loadFromFieldElements("man2");
        man3 = loadFromFieldElements("man3");
        tower = loadFromFieldElements("tower");
        strongTower = loadFromFieldElements("strong_tower");
        house = loadFromFieldElements("house");
    }


    public void onSkinChanged() {
        loadSkinDependentTextures();
    }


    private TextureRegion loadFromFieldElements(String name) {
        String fieldElementsFolderPath = menuViewYio.yioGdxGame.skinManager.getFieldElementsFolderPath();
        return GraphicsYio.loadTextureRegion(fieldElementsFolderPath + "/" + name + ".png", true);
    }


    @Override
    public void renderFirstLayer(InterfaceElement element) {

    }


    @Override
    public void renderSecondLayer(InterfaceElement element) {
        panel = (FastConstructionPanel) element;
        factor = panel.getFactor().get();
        pos = panel.viewPosition;

        GraphicsYio.setBatchAlpha(batch, factor);

        renderShadow();
        renderBackground();
        renderItems();

        GraphicsYio.setBatchAlpha(batch, 1);
    }


    private void renderShadow() {
        smDelta = 0.1f * h * (1 - factor);
        batch.draw(sideShadow, 0, -smDelta + 0.03f * h, w, 0.1f * h);
    }


    private void renderItems() {
        for (FcpItem item : panel.items) {
            if (!item.isVisible()) continue;

            GraphicsYio.drawFromCenter(
                    batch,
                    getItemTexture(item),
                    item.position.x,
                    item.position.y,
                    item.radius
            );

            if (item.isSelected()) {
                GraphicsYio.setBatchAlpha(batch, 0.5f * item.selectionFactor.get());

                GraphicsYio.drawFromCenter(
                        batch,
                        selectionPixel,
                        item.position.x,
                        item.position.y,
                        item.radius
                );

                GraphicsYio.setBatchAlpha(batch, factor);
            }
        }
    }


    private void renderBackground() {
        GraphicsYio.drawByRectangle(
                batch,
                backgroundTexture,
                pos
        );
    }


    private TextureRegion getItemTexture(FcpItem item) {
        if (item.action == FcpItem.ACTION_UNDO) {
            return undoIcon;
        }

        if (item.action == FcpItem.ACTION_END_TURN) {
            return endTurnIcon;
        }

        if (item.action == FcpItem.ACTION_DIPLOMACY) {
            if (menuViewYio.yioGdxGame.gameController.fieldController.diplomacyManager.log.hasSomethingToRead()) {
                return diplomacyRedIcon;
            } else {
                return diplomacyIcon;
            }
        }

        return getSkinDependentItemTexture(item);
    }


    private TextureRegion getSkinDependentItemTexture(FcpItem item) {
        switch (item.action) {
            default: return null;
            case FcpItem.ACTION_UNIT_1: return man0;
            case FcpItem.ACTION_UNIT_2: return man1;
            case FcpItem.ACTION_UNIT_3: return man2;
            case FcpItem.ACTION_UNIT_4: return man3;
            case FcpItem.ACTION_FARM: return house;
            case FcpItem.ACTION_TOWER: return tower;
            case FcpItem.ACTION_STRONG_TOWER: return strongTower;
        }
    }


    @Override
    public void renderThirdLayer(InterfaceElement element) {

    }
}
