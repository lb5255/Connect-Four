package connectfour.gui;

import connectfour.ConnectFourException;
import connectfour.client.ConnectFourBoard;
import connectfour.client.ConnectFourNetworkClient;
import connectfour.client.Observer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * A JavaFX GUI for the networked Connect Four game.
 *
 * @author James Heloitis @ RIT CS
 * @author Sean Strout @ RIT CS
 * @author Lea Boyadjian
 */
public class ConnectFourGUI extends Application implements Observer<ConnectFourBoard> {
    /**the model*/
    private ConnectFourBoard board;

    /** connection to network interface to server*/
    private ConnectFourNetworkClient serverConn;

    /**A global gridpane*/
    private GridPane gridpane = new GridPane();

    /**This label is dedicated for how many moves are left*/
    private Label movesleft;

    /**This label is dedicated for the end result of the game*/
    private Label decision;

    /** This is the first player's black button*/
    private Image p1 = new Image(getClass().getResourceAsStream("p1black.png"));

    /** This is the second player's red button*/
    private Image p2 = new Image(getClass().getResourceAsStream("p2red.png"));

    /** This is the image for the empty board */
    private Image empty_new = new Image(getClass().getResourceAsStream("empty.png"));


    /**
     * This sets everything up and ensures everything is ready to go
     */
    @Override
    public void init() {
        try {
            // get the command line args
            List<String> args = getParameters().getRaw();

            // get host info and port from command line
            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));

            //creates an empty board
            this.board = new ConnectFourBoard();

            // add ourselves as an observer
            this.board.addObserver(this);

            // Start the network client listener thread
            this.serverConn = new ConnectFourNetworkClient(host, port, this.board);

        } catch (NumberFormatException e) {
            System.err.println(e);
            throw new RuntimeException(e);
        } catch (ConnectFourException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construct the layout for the game.
     *
     * @param stage container (window) in which to render the GUI
     * @throws Exception if there is a problem
     */
    public void start(Stage stage) throws Exception {

        BorderPane borderpane = new BorderPane();
        movesleft = new Label(board.getMovesLeft() + " moves left.");
        decision = new Label("");

        for (int row = 0; row < board.ROWS; ++row) {
            for (int col = 0; col < board.COLS; ++col) {
                Button button = new Button("", new ImageView(empty_new));
                int number = col;
                button.setOnAction(actionEvent -> serverConn.sendMove(number));
                gridpane.add(button, col, row);
            }
        }

        HBox hBox = new HBox();
        hBox.getChildren().addAll(movesleft, decision);

        borderpane.setCenter(gridpane);
        borderpane.setBottom(hBox);

        Scene scene = new Scene(borderpane);
        stage.setTitle("Connect Four");
        stage.setScene(scene);
        stage.show();

        serverConn.startListener();
    }

    /**
     * GUI is closing, so close the network connection. Server will get the message.
     */
    @Override
    public void stop() {
        this.serverConn.close();
    }

    /**
     * Do your GUI updates here.
     */
    private void refresh() {
        movesleft.setText(board.getMovesLeft() + " moves left.  ");
        if (board.isMyTurn()) {
            turnon();
//            ConnectFourBoard.Status status = board.getStatus();
            for (int row = 0; row < board.ROWS; row++) {
                for (int col = 0; col < board.COLS; col++) {
                    if (board.getContents(col, row) == ConnectFourBoard.Move.PLAYER_ONE) {
                        Button p1button = new Button("", new ImageView(p1));
                        gridpane.add(p1button, col, row);
                    } else if (board.getContents(col, row) == ConnectFourBoard.Move.PLAYER_TWO) {
                        // update the buttons
                        Button p2button = new Button("", new ImageView(p2));
                        gridpane.add(p2button, col, row);
                    }
                }
            }
        } else {
            for (int row = 0; row < board.ROWS; ++row) {
                for (int col = 0; col < board.COLS; ++col) {
                    if (board.getContents(col, row) == ConnectFourBoard.Move.PLAYER_ONE) {
                        Button p1button = new Button("", new ImageView(p1));
                        gridpane.add(p1button, col, row);
                    } else if (board.getContents(col, row) == ConnectFourBoard.Move.PLAYER_TWO) {
                        Button p2button = new Button("", new ImageView(p2));
                        gridpane.add(p2button, col, row);
                    }
                }
            }
            turnoff();
        }
        ConnectFourBoard.Status status = board.getStatus();
        switch (status) {
            case ERROR:
                break;
            case I_WON:
                decision.setText("  You won. Yay! :D");
                break;
            case I_LOST:
                decision.setText("  You lost. Boo! :(");
                break;
            case TIE:
                decision.setText("  Tie game. Meh");
                break;
        }
    }

    /**
     * This disables the board that is not called to play
     */
    private void turnoff() {
        for(Node effect: gridpane.getChildren()){
            effect.setDisable(true);
        }
    }


    /**
     * This enables the board that is called to play
     */
    private void turnon() {
        for(Node effect: gridpane.getChildren()){
            effect.setDisable(false);
        }
    }
    /**
     * Called by the model, client.ConnectFourBoard, whenever there is a state change
     * that needs to be updated by the GUI.
     *
     * @param connectFourBoard
     */
    @Override
    public void update(ConnectFourBoard connectFourBoard) {
        if ( Platform.isFxApplicationThread() ) {
            this.refresh();
        }
        else {
            Platform.runLater( () -> this.refresh() );
        }
    }

    /**
     * The main method expects the host and port.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ConnectFourGUI host port");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}