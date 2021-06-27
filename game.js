const scene = {
    preload: function () {
        this.load.image("background", "assets/background.jpg");
    },
    create: function (){
        this.add.image(0, 0, "background").setOrigin(0, 0);

        let fur = this.add.rectangle(80, 100, 100, 100);
        fur.setStrokeStyle(4, 0xffffff);
        this.add.text(150, 100, "Шерсть", {fontSize: "25px"});
        let eyes = this.add.rectangle(80, 250, 100, 100);
        eyes.setStrokeStyle(4, 0xffffff);
        this.add.text(150, 250, "Глаза", {fontSize: "25px"});
        let collar = this.add.rectangle(80, 400, 100, 100);
        collar.setStrokeStyle(4, 0xffffff);
        this.add.text(150, 400, "Ошейник", {fontSize: "25px"});
        let cloak = this.add.rectangle(80, 550, 100, 100);
        cloak.setStrokeStyle(4, 0xffffff);
        this.add.text(150, 550, "Плащ", {fontSize: "25px"});

        let favorites = this.add.rectangle(820, 100, 100, 100);
        favorites.setStrokeStyle(4, 0xffffff);
        this.add.text(620, 100, "Избранное", {fontSize: "25px"});
        let sword = this.add.rectangle(820, 250, 100, 100);
        sword.setStrokeStyle(4, 0xffffff);
        this.add.text(700, 250, "Меч", {fontSize: "25px"});
        let flower = this.add.rectangle(820, 400, 100, 100);
        flower.setStrokeStyle(4, 0xffffff);
        this.add.text(660, 400, "Цветок", {fontSize: "25px"});
        let wings = this.add.rectangle(820, 550, 100, 100);
        wings.setStrokeStyle(4, 0xffffff);
        this.add.text(660, 550, "Крылья", {fontSize: "25px"});
    }
}

const config = {
    type: Phaser.AUTO,
    width: 900,
    height: 656,
    backgroundColor: "#000",
    parent: "game",
    pixelArt: true,
    scene: scene,
    physics: {
        default: "arcade",
        arcade: {
            gravity: { y: 0 }
        }
    }
};

const game = new Phaser.Game(config);