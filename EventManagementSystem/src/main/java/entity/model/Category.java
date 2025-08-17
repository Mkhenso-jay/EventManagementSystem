package entity.model;

public enum Category {
    ALL_EVENTS,
    MUSIC,
    GOSPEL,
    DANCE,
    ARTS_AND_THEATER,
    SPORTS,
    WORKSHOPS;

    public String getIconClass() {
        switch (this) {
            case ALL_EVENTS:
                return "fa-calendar-alt";
            case MUSIC:
                return "fa-music";
            case GOSPEL:
                return "fa-church";
            case DANCE:
                return "fa-user-friends";
            case ARTS_AND_THEATER:
                return "fa-theater-masks";
            case SPORTS:
                return "fa-futbol";
            case WORKSHOPS:
                return "fa-tools";
            default:
                return "fa-calendar";
        }
    }
}
