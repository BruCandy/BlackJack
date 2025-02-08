import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Main extends Application {
    private Group root;
    private Player player;
    private Player dealer;
    private int playerCardX, dealerCardX;
    private Text resultText;
    private Text playerScoreText;
    private Text dealerScoreText;
    private List<Card> deck;
    private List<ImageView> dealerCardViews = new ArrayList<>();
    private List<Card> dealerCards = new ArrayList<>();
    private boolean isAnimationRunning = false; 
    private boolean isGameActive = false;
    private boolean dealerCardsRevealed = false; 
    private boolean isResultLoading = false; 
    private ImageView titleImage; 


    @Override
    public void start(Stage st) throws Exception {
        root = new Group();
        Scene scene = new Scene(root, 800, 800, Color.BLACK);
        st.setTitle("BlackJack");
        st.setScene(scene);
        st.show();

        //スタート画面を作成
        titleImage = createImageView("project/title.gif", 700, 700, 100, -30);
        root.getChildren().add(titleImage);
        root.getChildren().add(createText("K, J, Q are worth 10, and A is worth 1 or 11", 180, 620, 20, Color.WHITE));
        Text startText = createText("Press SPACE to Start the Game", 200, 530, 24, Color.YELLOW);
        root.getChildren().add(startText);
        root.getChildren().add(createText("Press SPACE to add a card, and Press UP to challenge", 140, 580, 20, Color.WHITE));
        addCardExplanationImages();

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            private boolean visible = true;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 500_000_000) { 
                    visible = !visible;
                    startText.setVisible(visible);
                    lastUpdate = now;
                }
            }
        };
        timer.start();

        setTitleScreenEvents(scene);
    }


    private Text createText(String content, double x, double y, int fontSize, Color color) {
        Text text = new Text(x, y, content);
        text.setFont(new Font("Arial", fontSize));
        text.setFill(color);
        return text;
    }


    private ImageView createImageView(String path, double width, double height, double x, double y) {
        ImageView imageView = new ImageView(new Image(new File(path).toURI().toString()));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setTranslateX(x);
        imageView.setTranslateY(y);
        return imageView;
    }


    private void addCardExplanationImages() {
        // クラブのキング
        root.getChildren().add(createImageView("project/cards/club/club13.gif", 75, 105, 100, 650));
        root.getChildren().add(createText("= 10",170, 710, 18, Color.WHITE));

        // ダイヤのクイーン
        root.getChildren().add(createImageView("project/cards/diamond/diamond12.gif", 75, 105, 240, 650));
        root.getChildren().add(createText("= 10",305, 710, 18, Color.WHITE));

        // スペードのジャック
        root.getChildren().add(createImageView("project/cards/spade/spade11.gif", 75, 105, 375, 650));
        root.getChildren().add(createText("= 10",440, 710, 18, Color.WHITE));

        // ハートのエース
        root.getChildren().add(createImageView("project/cards/heart/heart1.gif", 75, 105, 510, 650));
        root.getChildren().add(createText("= 11 or 1",575, 710, 18, Color.WHITE));
    }


    public void setTitleScreenEvents(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (isAnimationRunning) return;

            if (e.getCode() == KeyCode.SPACE) {
                startGame();
            }
        });
    }


    public void startGame() {
        root.getChildren().clear();
        player = new Player();
        dealer = new Player();
        playerCardX = 150;
        dealerCardX = 150;
        isGameActive = true;
        dealerCardsRevealed = false;
        isResultLoading = false;

        Scene scene = root.getScene();
        scene.setFill(Color.GREEN);

        initializeDeck();

        playerScoreText = createText("", 700, 500, 18, Color.WHITE);
        dealerScoreText = createText("Dealer: ?", 700, 200, 18, Color.WHITE);
        resultText = createText("", 100, 400, 18, Color.WHITE);
        root.getChildren().addAll(playerScoreText, dealerScoreText, resultText);

        setupHandAnimation(-50, 550, 300, 300, false); 
        setupHandAnimation(500, -50, 300, 300, true); 

        addKeyExplanations();

        addGameExplanation(); 

        drawCard(player, playerCardX, 500);
        drawDealerCardBack(dealerCardX, 200);

        updateScores();

        setGameScreenEvents(scene);
    }


    public void initializeDeck() {
        deck = new ArrayList<>();
        String[] suits = {"heart", "club", "spade", "diamond"};
        for (int i = 0; i < 4; i++) {
            String suit = suits[i];
            for (int j = 1; j <= 13; j++) {
                Card card = new Card(j, suit);
                deck.add(card);
            }
        }
        Collections.shuffle(deck);
    }


    public void setupHandAnimation(double x, double y, double width, double height, boolean flip) {
        ImageView handImage = new ImageView();
        handImage.setFitWidth(width);
        handImage.setFitHeight(height);
        handImage.setTranslateX(x);
        handImage.setTranslateY(y);
        if (flip) {
            handImage.setScaleX(-1);
            handImage.setScaleY(-1);
        }
        root.getChildren().add(handImage);

        List<Image> handImages = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            handImages.add(new Image(new File("project/hand/hand" + i + ".gif").toURI().toString()));
        }

        AnimationTimer timer = new AnimationTimer() {
            private int frame = 0;
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 250_000_000) {
                    handImage.setImage(handImages.get(frame % handImages.size()));
                    frame++;
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }


    private void addKeyExplanations() {
        addKeyExplanation("project/key/spacekey.gif", "Press SPACE to add a card", 400, 550, 550, 680);
        addKeyExplanation("project/key/upkey.gif", "Press UP to challenge", 305, 670, 550, 720);
        addKeyExplanation("project/key/downkey.gif", "Press DOWN to return to title\nYou can press after the game ends", 305, 685, 550, 755);
    }

    private void addKeyExplanation(String imagePath, String text, double x, double y, double textX, double textY) {
        root.getChildren().add(createImageView(imagePath, 200, 150, x, y));
        root.getChildren().add(createText(text, textX, textY, 14, Color.WHITE));
    }


    private void addGameExplanation() {
        root.getChildren().add(createImageView("project/frame.gif", 410, 280, 0, -40));
        root.getChildren().add(createText("Try to get as close to 21 as possible\nwithout going over.", 55, 120, 17, Color.BLACK));
    }


    public void drawCard(Player player, int x, int y) {
        if (deck.isEmpty() || isAnimationRunning) return;

        Card card = deck.remove(0);
        player.addCard(card.getValue());

        ImageView cardImage = card.getCardImage();
        cardImage.setTranslateX(850);
        cardImage.setTranslateY(y);

        root.getChildren().add(cardImage);

        TranslateTransition move = new TranslateTransition(Duration.seconds(0.5), cardImage);
        move.setToX(x);
        move.setToY(y);

        isAnimationRunning = true;
        move.setOnFinished(event -> {
            isAnimationRunning = false;
            updateScores();
            checkBust();
        });
        move.play();
    }


    public void updateScores() {
        playerScoreText.setText("Player: " + player.getTotalValue());
        if (dealerCardsRevealed) {
            dealerScoreText.setText("Dealer: " + dealer.getTotalValue());
        } else {
            dealerScoreText.setText("Dealer: ?");
        }
    }


    public void checkBust() {
        if (player.getTotalValue() > 21) {
            resultText.setText("Player Bust! Press UP to see the result.");
            isGameActive = false;
            dealerCardsRevealed = false;
        }
    }


    public void drawDealerCardBack(int x, int y) {
        if (deck.isEmpty()) return;

        Card card = deck.remove(0);

        ImageView cardBack = createImageView("project/cards/card1.gif", 75, 105, 850, y);

        TranslateTransition move = new TranslateTransition(Duration.seconds(0.5), cardBack);
        move.setToX(x);
        move.setToY(y);

        isAnimationRunning = true;

        move.setOnFinished(event -> {
            dealer.addCard(card.getValue());
            dealerCardViews.add(cardBack);
            isAnimationRunning = false;
        });

        dealerCards.add(card);
        root.getChildren().add(cardBack);
        move.play();
    }


    public void setGameScreenEvents(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (isAnimationRunning) return;

            if (e.getCode() == KeyCode.SPACE && isGameActive && !isResultLoading) {
                drawCard(player, playerCardX += 60, 500);
            } else if (e.getCode() == KeyCode.UP && !dealerCardsRevealed) { 
                dealerTurnAndReveal(); 
            } else if (e.getCode() == KeyCode.DOWN && dealerCardsRevealed) {
                resetToTitle();
            }
        });
    }


    public void dealerTurnAndReveal() {
        if (isAnimationRunning || dealerCardsRevealed) return;

        isAnimationRunning = true;
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 500_000_000) {
                    if (dealer.getTotalValue() <= 16) {
                        drawDealerCardBack(dealerCardX += 60, 200);
                        updateScores();
                    } else {
                        stop();
                        isAnimationRunning = false;
                        revealDealerCards();
                    }
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }


    public void revealDealerCards() {
        isAnimationRunning = true;
        AnimationTimer timer1 = new AnimationTimer() {
            private int cardIndex = 0;
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 100_000_000 && cardIndex < dealerCardViews.size()) {
                    ImageView cardView = dealerCardViews.get(cardIndex);
                    Card card = dealerCards.get(cardIndex);

                    if (card == null) {
                        stop();
                        return;
                    }

                    AnimationTimer timer2 = new AnimationTimer() {
                        private int frame = 1;
                        private long frameLastUpdate = 0;

                        @Override
                        public void handle(long now) {
                            if (now - frameLastUpdate >= 100_000_000 && frame <= 10) {
                                cardView.setImage(new Image(new File("project/cards/card" + frame + ".gif").toURI().toString()));
                                frame++;
                                frameLastUpdate = now;
                            } else if (frame > 10) {
                                cardView.setImage(card.getCardImage().getImage());
                                stop();
                            }
                        }
                    };
                    timer2.start();

                    cardIndex++;
                    lastUpdate = now;
                }

                if (cardIndex >= dealerCardViews.size()) {
                    stop();
                    dealerCardsRevealed = true;
                    isAnimationRunning = false;
                    isResultLoading = false;
                    dealerScoreText.setText("Dealer: " + dealer.getTotalValue());
                    finalizeGame();
                }
            }
        };
        timer1.start();
    }


    public void finalizeGame() {
        int playerTotal = player.getTotalValue();
        int dealerTotal = dealer.getTotalValue();

        if (playerTotal > 21 && dealerTotal > 21) {
            resultText.setText("Both Bust! It's a Tie!\nPress DOWN to return to Title");
        } else if (playerTotal > 21) {
            resultText.setText("Player Bust! Dealer Wins!\nPress DOWN to return to Title");
        } else if (dealerTotal > 21) {
            resultText.setText("Dealer Bust! Player Wins!\nPress DOWN to return to Title");
        } else if (playerTotal > dealerTotal) {
            resultText.setText("Player Wins!\nPress DOWN to return to Title");
        } else if (dealerTotal > playerTotal) {
            resultText.setText("Dealer Wins!\nPress DOWN to return to Title");
        } else {
            resultText.setText("It's a Tie!\nPress DOWN to return to Title");
        }

        isGameActive = false;
        dealerCardsRevealed = true;
    }


    public void resetToTitle() {
        root.getChildren().clear();

        // スタート画面を再設定
        root.getChildren().add(titleImage);
        root.getChildren().add(createText("K, J, Q are worth 10, and A is worth 1 or 11", 180, 620, 20, Color.WHITE));
        Text startText = createText("Press SPACE to Start the Game", 200, 530, 24, Color.YELLOW);
        root.getChildren().add(startText);
        root.getChildren().add(createText("Press SPACE to add a card, and Press UP to challenge", 140, 580, 20, Color.WHITE));
        addCardExplanationImages();

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            private boolean visible = true;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 500_000_000) {
                    visible = !visible;
                    startText.setVisible(visible);
                    lastUpdate = now;
                }
            }
        };
        timer.start();

        dealerCardViews.clear();

        Scene scene = root.getScene();
        scene.setFill(Color.BLACK);

        setTitleScreenEvents(scene);
    }


    public static void main(String[] a) {
        launch(a);
    }
}


class Player {
    private List<Integer> hand;
    private int totalScore;

    Player() {
        hand = new ArrayList<>();
        totalScore = 0;
    }

    public void addCard(int cardValue) {
        hand.add(cardValue);
        totalScore = calculateBestScore();
    }

    public int calculateBestScore() {
        List<Integer> scores = new ArrayList<>();
        calculateScoreCombinations(scores, 0, 0);

        return scores.stream()
                .filter(score -> score <= 21)
                .max(Integer::compare)
                .orElseGet(() -> scores.stream().min(Integer::compare).orElse(0));
    }

    private void calculateScoreCombinations(List<Integer> scores, int index, int currentSum) {
        if (index == hand.size()) {
            scores.add(currentSum);
            return;
        }

        int cardValue = hand.get(index);
        if (cardValue == 1) { 
            calculateScoreCombinations(scores, index + 1, currentSum + 1);
            calculateScoreCombinations(scores, index + 1, currentSum + 11);
        } else {
            calculateScoreCombinations(scores, index + 1, currentSum + Math.min(cardValue, 10));
        }
    }

    public int getTotalValue() {
        return totalScore;
    }
}


class Card {
    private int value;
    private String suit;

    Card(int value, String suit) {
        this.value = value;
        this.suit = suit;
    }

    public int getValue() {
        return value;
    }

    public String getSuit() {
        return suit;
    }

    public ImageView getCardImage() {
        String imagePath = "project/cards/" + suit + "/" + suit + value + ".gif";
        ImageView cardImage = new ImageView(new Image(new File(imagePath).toURI().toString()));
        cardImage.setFitWidth(75);
        cardImage.setFitHeight(105);
        return cardImage;
    }
}
