package yio.tro.antiyoy.menu.diplomatic_dialogs;

import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomacyManager;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomaticEntity;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.LanguagesManager;

import java.util.ArrayList;

public class HexSaleDialog extends AbstractDiplomaticDialog {

    DiplomaticEntity sender, recipient;
    ArrayList<Hex> hexesToSell;


    public HexSaleDialog(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        hexesToSell = new ArrayList<>();
        resetEntities();
    }


    @Override
    protected void onAppear() {
        super.onAppear();
        resetEntities();
    }


    protected void resetEntities() {
        sender = null;
        recipient = null;
    }


    public void setData(DiplomaticEntity recipient, ArrayList<Hex> hexesToSell) {
        this.recipient = recipient;

        Hex firstHex = hexesToSell.get(0);
        sender = getDiplomacyManager().getEntity(firstHex.colorIndex);

        this.hexesToSell.clear();
        this.hexesToSell.addAll(hexesToSell);

        updateAll();
        updateTagColor();
    }


    private void updateTagColor() {
        setTagColor(recipient.color);
    }


    @Override
    protected void updateTagPosition() {
        tagPosition.x = viewPosition.x + leftOffset;
        tagPosition.width = viewPosition.width - 2 * leftOffset;
        tagPosition.y = viewPosition.y + viewPosition.height - topOffset - titleOffset - lineOffset + lineOffset / 4;
        tagPosition.height = lineOffset;
    }


    @Override
    protected void makeLabels() {
        if (sender == null) return;
        if (recipient == null) return;

        LanguagesManager instance = LanguagesManager.getInstance();
        float y = (float) (position.height - topOffset);

        addLabel(instance.getString("hex_sale"), Fonts.gameFont, leftOffset, y);
        y -= titleOffset;

        addLabel(instance.getString("state") + ": " + recipient.capitalName, Fonts.smallerMenuFont, leftOffset, y);
        y -= lineOffset;

        addLabel(instance.getString("quantity") + ": " + hexesToSell.size(), Fonts.smallerMenuFont, leftOffset, y);
        y -= lineOffset;

        addLabel(instance.getString("price") + ": $" + getMoneyString(), Fonts.smallerMenuFont, leftOffset, y);
        y -= lineOffset;
    }


    private String getMoneyString() {
        return getPrice() + "";
    }


    private int getPrice() {
        return getDiplomacyManager().calculatePriceForHexes(hexesToSell);
    }


    private DiplomacyManager getDiplomacyManager() {
        return menuControllerYio.yioGdxGame.gameController.fieldController.diplomacyManager;
    }


    @Override
    protected void onYesButtonPressed() {
        getDiplomacyManager().onEntityRequestedHexSell(sender, recipient, hexesToSell, getPrice());
        destroy();
    }


    @Override
    protected void onNoButtonPressed() {

    }


    @Override
    public boolean isInSingleButtonMode() {
        return true;
    }


    @Override
    public boolean areButtonsEnabled() {
        return true;
    }
}
