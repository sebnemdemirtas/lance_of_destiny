package org.Controllers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.mongodb.client.MongoCollection;
import org.Domain.BarrierType;
import org.Domain.Coordinate;
import org.Domain.Game;
import org.Domain.*;
import org.Utils.Database;
import org.bson.Document;
import java.util.ArrayList;

import java.util.HashMap;
public class DataBaseController {
    private static Game gameSession;
    private static DataBaseController instance;
    public DataBaseController(){
        this.gameSession = Game.getInstance();
    }
    public void openFromDatabase(Document game){
        Game gameInstance = Game.getInstance();
        gameInstance.reset();
        String templateName = game.getString("gameName");
        String gameDate = game.getString("gameDate");
        gameInstance.setGameName(templateName);
        gameInstance.setDate(gameDate);
        int chancesLeft=game.getInteger("chancesLeft");
        gameInstance.setChance(chancesLeft);
        int barrierAmount=game.getInteger("barrierAmount");
        for(int j=0; j<barrierAmount; j++) {
            String barrierInfo = game.getString("barrier_" + j);
            String[] parts = barrierInfo.split("/");
            // Extracting information from parts array
            int xCoordinate = Integer.parseInt(parts[0]);
            int yCoordinate = Integer.parseInt(parts[1]);
            BarrierType barrierType = BarrierType.valueOf(parts[2]);
            int numHits = Integer.parseInt(parts[3]);
            boolean isMoving= Boolean.parseBoolean(parts[4]);
            int velocity = Integer.parseInt(parts[5]);
            Coordinate co  =new Coordinate(xCoordinate, yCoordinate);
            gameInstance.addDetailedBarrierFromDb(co, barrierType, numHits, isMoving, velocity);
        }
        gameInstance.getInventory().put(SpellType.FELIX_FELICIS, game.getInteger("spellFelixFelicis"));
        gameInstance.getInventory().put(SpellType.STAFF_EXPANSION, game.getInteger("spellStaffExpansion"));
        gameInstance.getInventory().put(SpellType.HEX, game.getInteger("spellHex"));
        gameInstance.getInventory().put(SpellType.OVERWHELMING_FIREBALL, game.getInteger("spellOverwhelming"));
        String[] fireballParts = game.getString("fireball").split("/");
        gameInstance.getFireball().getCoordinate().setX(Integer.parseInt(fireballParts[0]));
        gameInstance.getFireball().getCoordinate().setY(Integer.parseInt(fireballParts[1]));
        gameInstance.getFireball().setxVelocity(Float.parseFloat(fireballParts[2]));
        gameInstance.getFireball().setyVelocity(Float.parseFloat(fireballParts[3]));

    }
    public void saveGameToDatabase(String gameName, Game game, boolean played) {
        // Get the current date and time with time zone
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        // Format the date and time
        String formattedDateTime = now.format(formatter);
        game.setDate(formattedDateTime);
        ArrayList<Barrier> barriers = game.getBarriers();
        Document gameSession = new Document();
        gameSession.put("email", User.getUserInstance().getEmail());
        gameSession.put("gameName", gameName);
        gameSession.put("gameDate", game.getDate());
        gameSession.put("barrierAmount", barriers.size());
        gameSession.put("chancesLeft", game.getChance().getRemainingChance());
        for(int i=0; i<barriers.size(); i++){
            gameSession.put("barrier_"+i, barriers.get(i).getCoordinate().getX() + "/"+barriers.get(i).getCoordinate().getY() +
                    "/"+ barriers.get(i).getType().toString()+ "/" + barriers.get(i).getnHits() +
                    "/"+ barriers.get(i).isMoving() + "/"+barriers.get(i).getVelocity() );
        }
        HashMap<SpellType, Integer> inventory = game.getInventory();
        gameSession.put("spellFelixFelicis",inventory.get(SpellType.FELIX_FELICIS));
        gameSession.put("spellStaffExpansion",inventory.get(SpellType.STAFF_EXPANSION));
        gameSession.put("spellHex",inventory.get(SpellType.HEX));
        gameSession.put("spellOverwhelming",inventory.get(SpellType.OVERWHELMING_FIREBALL));
        Fireball fireball = game.getFireball();
        gameSession.put("fireball", fireball.getCoordinate().getX() + "/"+
                fireball.getCoordinate().getY() + "/" +
                fireball.getxVelocity()+ "/" +
                fireball.getyVelocity());
        if(played)gameSession.put("played", "True");
        else gameSession.put("played", "False");
        Database.getInstance().getGameCollection().insertOne(gameSession);
        System.out.println("Saved");
    }

    public void openMultiplayerGame(String gameName){
        Document game = getGame(gameName);
        Game gameInstance = Game.getInstance();
        gameInstance.reset();
        String templateName = game.getString("gameName");
        int barrierAmount=game.getInteger("barrierAmount");
        for(int j=0; j<barrierAmount; j++) {
            String barrierInfo = game.getString("barrier_" + j);
            String[] parts = barrierInfo.split("/");
            // Extracting information from parts array
            int xCoordinate = Integer.parseInt(parts[0]);
            int yCoordinate = Integer.parseInt(parts[1]);
            BarrierType barrierType = BarrierType.valueOf(parts[2]);
            int numHits = Integer.parseInt(parts[3]);
            boolean isMoving= Boolean.parseBoolean(parts[4]);
            int velocity = Integer.parseInt(parts[5]);
            Coordinate co  =new Coordinate(xCoordinate, yCoordinate);
            gameInstance.addDetailedBarrier(co, barrierType, numHits, isMoving, velocity);
        }
    }

    public Document getGame(String gameName) {
        MongoCollection<Document> gameCollection = Database.getInstance().getGameCollection();
        Document query = new Document("gameName", gameName);
        Document game = gameCollection.find(query).first();
        return game;
    }

    public static DataBaseController getInstance(){
        if (instance==null) {
            instance=new DataBaseController();
            return instance;
        }
        else{
            return instance;
        }
    }
}