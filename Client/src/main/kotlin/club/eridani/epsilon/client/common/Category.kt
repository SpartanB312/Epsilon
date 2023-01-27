package club.eridani.epsilon.client.common

enum class Category(val showName: String, val iconCode: String = "", val isHUD: Boolean = false) {

    //Cheat
    Client("Client", "9"),
    Combat("Combat", "b"),
    Misc("Misc", "["),
    Movement("Movement", "@"),
    Player("Player", "}"),
    Render("Render", "a"),

    //HUD Editor
    CombatHUD("CombatHUD", isHUD = true),
    InfoHUD("Information", isHUD = true),
    SpartanHUD("Spartan", isHUD = true),

    //Hidden
    Hidden("Hidden"),
    Setting("Setting", "8")

}
