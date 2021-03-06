package yio.tro.antiyoy.gameplay.diplomacy;

import yio.tro.antiyoy.YioGdxGame;
import yio.tro.antiyoy.gameplay.DebugFlags;
import yio.tro.antiyoy.gameplay.FieldController;
import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.gameplay.Province;

import java.util.ArrayList;

public class DiplomaticAI {

    DiplomacyManager diplomacyManager;
    private ArrayList<Province> tempProvinceList;
    private ArrayList<Hex> propagationList;


    public DiplomaticAI(DiplomacyManager diplomacyManager) {
        this.diplomacyManager = diplomacyManager;

        tempProvinceList = new ArrayList<>();
        propagationList = new ArrayList<>();
    }


    void checkToChangeRelations() {
        DiplomaticEntity mainEntity = getMainEntity();
        if (mainEntity.isHuman()) return;
        if (!mainEntity.alive) return;

        mainEntity.thinkAboutChangingRelations();
    }


    void onAiTurnStarted() {
        if (!getMainEntity().alive) return;

        aiProcessMessages();
        aiSendMessages();
    }


    private void aiSendMessages() {
        if (YioGdxGame.random.nextInt(8) == 0) {
            performAiToHumanFriendshipProposal();
        }

        if (YioGdxGame.random.nextInt(4) == 0) {
            performAiToHumanBlackMark();
        }

        if (getMainEntity().getStateFullMoney() > 100 && YioGdxGame.random.nextInt(15) == 0) {
            performAiToHumanGift();
        }

        if (DebugFlags.cheatCharisma) {
            applyCharismaCheat();
        }

        if (getMainEntity().getStateBalance() > 50 && YioGdxGame.random.nextInt(4) == 0) {
            performAiToHumanHexBuyProposal();
        }

        if (getMainEntity().getStateBalance() < 9 && YioGdxGame.random.nextInt(4) == 0) {
            performAiToHumanHexSellProposal();
        }
    }


    private void performAiToHumanHexSellProposal() {
        DiplomaticEntity humanFriend = getRandomHumanFriend();
        if (humanFriend == null) return;

        Province province = getRandomProvinceToTradeHexes(getMainEntity().color);
        if (province == null) return;

        preparePropagationListToTrade(province);

        getLog().addMessage(DipMessageType.hex_sale, getMainEntity(), humanFriend)
                .setArg1(diplomacyManager.convertHexListToString(propagationList))
                .setArg2("" + diplomacyManager.calculatePriceForHexes(propagationList));
    }


    private void performAiToHumanHexBuyProposal() {
        DiplomaticEntity humanFriend = getRandomHumanFriend();
        if (humanFriend == null) return;

        Province province = getRandomProvinceToTradeHexes(humanFriend.color);
        if (province == null) return;

        preparePropagationListToTrade(province);

        getLog().addMessage(DipMessageType.hex_purchase, getMainEntity(), humanFriend)
                .setArg1(diplomacyManager.convertHexListToString(propagationList))
                .setArg2("" + diplomacyManager.calculatePriceForHexes(propagationList));
    }


    private void preparePropagationListToTrade(Province province) {
        propagationList.clear();

        Hex randomHex = province.getRandomHex();
        propagationList.add(randomHex);

        int goalQuantity = YioGdxGame.random.nextInt(4) + 3;
        if (goalQuantity > province.hexList.size()) {
            goalQuantity = province.hexList.size();
        }

        while (propagationList.size() < goalQuantity) {
            Hex newHex = getRandomHexNearPropagationList(province);
            propagationList.add(newHex);
        }
    }


    private Hex getRandomHexNearPropagationList(Province province) {
        while (true) {
            Hex randomHex = province.getRandomHex();
            if (propagationList.contains(randomHex)) continue;
            if (!isHexNearPropagationList(randomHex)) continue;

            return randomHex;
        }
    }


    private boolean isHexNearPropagationList(Hex hex) {
        for (int dir = 0; dir < 6; dir++) {
            Hex adjacentHex = hex.getAdjacentHex(dir);
            if (adjacentHex == null) continue;
            if (adjacentHex.isNullHex()) continue;
            if (!propagationList.contains(adjacentHex)) continue;

            return true;
        }

        return false;
    }


    private Province getRandomProvinceToTradeHexes(int filterColor) {
        int c = 1000;

        while (c > 0) {
            c--;

            Province randomProvince = getFieldController().getRandomProvince();
            if (randomProvince.getColor() != filterColor) continue;

            return randomProvince;
        }

        return null;
    }


    private DiplomaticEntity getRandomHumanFriend() {
        if (!hasAtLeastOnceHumanFriend()) return null;

        while (true) {
            DiplomaticEntity randomHumanEntity = getRandomHumanEntity();
            if (!getMainEntity().isFriendTo(randomHumanEntity)) continue;

            return randomHumanEntity;
        }
    }


    public boolean hasAtLeastOnceHumanFriend() {
        for (DiplomaticEntity entity : getEntities()) {
            if (!entity.isHuman()) continue;
            if (!getMainEntity().isFriendTo(entity)) continue;

            return true;
        }

        return false;
    }


    private void applyCharismaCheat() {
        DiplomaticEntity mainEntity = getMainEntity();
        DiplomaticEntity randomHumanEntity = getRandomHumanEntity();
        if (randomHumanEntity == null) return;

        diplomacyManager.transferMoney(mainEntity, randomHumanEntity, mainEntity.getStateFullMoney() / 2);
    }


    private void performAiToHumanGift() {
        DiplomaticEntity mainEntity = getMainEntity();
        DiplomaticEntity randomHumanEntity = getRandomHumanEntity();
        if (randomHumanEntity == null) return;
        if (mainEntity.getRelation(randomHumanEntity) == DiplomaticRelation.ENEMY) return;
        if (mainEntity.isBlackMarkedWith(randomHumanEntity)) return;

        diplomacyManager.transferMoney(mainEntity, randomHumanEntity, 10 + YioGdxGame.random.nextInt(11));
    }


    private void aiProcessMessages() {
        DiplomaticEntity mainEntity = getMainEntity();

        for (int i = getLog().messages.size() - 1; i >= 0; i--) {
            DiplomaticMessage message = getLog().messages.get(i);

            if (message.recipient != mainEntity) continue;

            switch (message.type) {
                case friendship_proposal:
                    if (diplomacyManager.isFriendshipPossible(message.sender, message.recipient)) {
                        diplomacyManager.makeFriends(message.sender, message.recipient);
                    }
                    break;
                case stop_war:
                    diplomacyManager.onEntityRequestedToStopWar(message.sender, message.recipient);
                    break;
                case hex_purchase:
                    if (doesAiAllowToBuyItsHexes(message)) {
                        diplomacyManager.applyHexPurchase(message);
                    }
                    break;
                case hex_sale:
                    if (doesAiWantToBuyOthersHexes(message)) {
                        diplomacyManager.applyHexPurchase(message);
                    }
                    break;
            }

            getLog().removeMessage(message);
        }
    }


    private boolean doesAiWantToBuyOthersHexes(DiplomaticMessage message) {
        int price = Integer.valueOf(message.arg2);
        DiplomaticEntity buyer = message.recipient;
        return buyer.getStateFullMoney() > price && buyer.getStateBalance() > 0;
    }


    private boolean doesAiAllowToBuyItsHexes(DiplomaticMessage message) {
        DiplomaticEntity buyer = message.sender;
        DiplomaticEntity seller = message.recipient;
        ArrayList<Hex> hexList = diplomacyManager.convertStringToPurchaseList(message.arg1);

        tempProvinceList.clear();
        for (Hex hex : hexList) {
            Province provinceByHex = getFieldController().getProvinceByHex(hex);
            if (provinceByHex == null) continue;
            if (tempProvinceList.contains(provinceByHex)) continue;
            tempProvinceList.add(provinceByHex);
        }

        for (Province province : tempProvinceList) {
            if (doesHexListSplitProvince(province, hexList)) {
                return false;
            }
        }

        return true;
    }


    private boolean doesHexListSplitProvince(Province province, ArrayList<Hex> restrictionList) {
        for (Hex hex : province.hexList) {
            hex.flag = false;
        }

        propagationList.clear();
        propagationList.add(province.hexList.get(0));

        while (propagationList.size() > 0) {
            Hex hex = propagationList.get(0);
            propagationList.remove(0);
            hex.flag = true;

            for (int dir = 0; dir < 6; dir++) {
                Hex adjacentHex = hex.getAdjacentHex(dir);
                if (adjacentHex == null) continue;
                if (adjacentHex == getFieldController().nullHex) continue;
                if (!adjacentHex.active) continue;
                if (!adjacentHex.sameColor(hex)) continue;
                if (adjacentHex.flag) continue;
                if (restrictionList.contains(adjacentHex)) continue;

                propagationList.add(adjacentHex);
            }
        }

        for (Hex hex : province.hexList) {
            if (hex.flag) continue;
            if (restrictionList.contains(hex)) continue;
            return true;
        }

        for (Hex hex : restrictionList) {
            if (hex == province.hexList.get(0)) {
                hex.flag = false;
            }
        }

        if (getFlaggedHexesQuantity(province.hexList) < 2) return true; // 1 hex is flagged by default

        return false;
    }


    private FieldController getFieldController() {
        return diplomacyManager.fieldController;
    }


    private int getFlaggedHexesQuantity(ArrayList<Hex> list) {
        int c = 0;

        for (Hex hex : list) {
            if (!hex.flag) continue;
            c++;
        }

        return c;
    }


    public void performAiToHumanBlackMark() {
        DiplomaticEntity aiEntity = findAiEntityThatIsCloseToWin();
        if (aiEntity == null) return;

        DiplomaticEntity randomHumanEntity = getRandomHumanEntity();
        if (randomHumanEntity == null) return;

        int relation = aiEntity.getRelation(randomHumanEntity);
        if (relation == DiplomaticRelation.FRIEND) return;
        if (randomHumanEntity.isBlackMarkedWith(aiEntity)) return;

        getLog().addMessage(DipMessageType.black_marked, aiEntity, randomHumanEntity);

        diplomacyManager.makeBlackMarked(aiEntity, randomHumanEntity);
    }


    public boolean performAiToHumanFriendshipProposal() {
        DiplomaticEntity humanEntity = getRandomHumanEntity();
        if (humanEntity == null) return false;
        if (humanEntity.isOneFriendAwayFromDiplomaticVictory()) return false;
        if (!humanEntity.alive) return false;

        for (int i = 0; i < 25; i++) {
            DiplomaticEntity randomEntity = getRandomEntity();
            if (!randomEntity.alive) continue;
            if (randomEntity.isHuman()) continue;
            if (randomEntity.isOneFriendAwayFromDiplomaticVictory()) continue; // no tricky friend requests

            int relation = humanEntity.getRelation(randomEntity);
            if (relation != DiplomaticRelation.NEUTRAL) continue;
            if (!humanEntity.acceptsFriendsRequest(randomEntity)) continue;

            getLog().addMessage(DipMessageType.friendship_proposal, randomEntity, humanEntity);
            return true;
        }

        return false;
    }


    public DiplomaticEntity findAiEntityThatIsCloseToWin() {
        for (DiplomaticEntity entity : getEntities()) {
            if (entity.isHuman()) continue;
            if (!entity.alive) continue;

            if (entity.isOneFriendAwayFromDiplomaticVictory()) {
                return entity;
            }
        }

        return null;
    }


    public DiplomaticEntity getRandomHumanEntity() {
        if (!isAtLeastOneHumanEntity()) return null;

        while (true) {
            DiplomaticEntity randomEntity = getRandomEntity();
            if (randomEntity.isHuman()) {
                return randomEntity;
            }
        }
    }


    private boolean isAtLeastOneHumanEntity() {
        for (DiplomaticEntity entity : getEntities()) {
            if (entity.isHuman()) {
                return true;
            }
        }

        return false;
    }


    private DiplomaticLog getLog() {
        return diplomacyManager.log;
    }


    private ArrayList<DiplomaticEntity> getEntities() {
        return diplomacyManager.entities;
    }


    private DiplomaticEntity getMainEntity() {
        return diplomacyManager.getMainEntity();
    }


    public DiplomaticEntity getRandomEntity() {
        return diplomacyManager.getRandomEntity();
    }
}
