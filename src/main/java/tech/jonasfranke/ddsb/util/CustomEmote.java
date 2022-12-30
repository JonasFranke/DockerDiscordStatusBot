package tech.jonasfranke.ddsb.util;

public enum CustomEmote {
    GreenUpArrow("GreenUpArrow", "1058388762121490542");// Adding a \ in front of the emote such as \:Dog_Spin: will display the name along with the ID such as  <a:Dog_Spin:801604978040897568>


    private final String id;
    private final String fullString;
    private final String name;
    CustomEmote(String name, String id) {
        this.id = id;
        this.name = name;
        fullString = "<:" + name +":1058388762121490542>";
    }

    public String getId() {
        return id;
    }

    public String getFullString() {
        return fullString;
    }

    public String getName() {
        return name;
    }
}
