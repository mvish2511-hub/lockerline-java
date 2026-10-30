package com.lockerline.error;

/** The example error bodies the API docs show for each refusal - the exact shape a client will receive. */
public final class ProblemExamples {

    public static final String PROBLEM = "application/problem+json";

    public static final String VALIDATION_QUERY = """
            {"type":"/errors/validation","title":"Invalid request","status":400,"detail":"The request is not valid",\
            "instance":"/towers/A/doors","errors":[{"field":"size","message":"'XL' is not an allowed value"}]}""";
    public static final String VALIDATION_DROP = """
            {"type":"/errors/validation","title":"Invalid request","status":400,"detail":"The request is not valid",\
            "instance":"/drops","errors":[{"field":"sizeClass","message":"this value cannot be read"}]}""";
    public static final String NO_SUCH_TOWER = """
            {"type":"/errors/no-such-tower","title":"No such tower","status":404,"detail":"There is no tower Z",\
            "instance":"/towers/Z/doors"}""";
    public static final String NO_SUCH_DOOR = """
            {"type":"/errors/no-such-door","title":"No such door","status":404,"detail":"Tower A has no door D99",\
            "instance":"/towers/A/doors/D99"}""";
    public static final String NO_SUCH_FLAT = """
            {"type":"/errors/no-such-flat","title":"No such flat","status":400,\
            "detail":"There is no flat 999 in this tower","instance":"/drops"}""";
 public static final String NO_DOOR_FREE = """
            {"type":"/errors/no-door-free","title":"No door free","status":409,"detail":"No L door is free",\
            "instance":"/drops","freeSizes":["S","M"]}""";
    public static final String NO_SUCH_PARCEL = """
            {"type":"/errors/no-such-parcel","title":"No such parcel","status":404,"detail":"There is no parcel P999",\
            "instance":"/parcels/P999"}""";
    public static final String NO_SUCH_CODE = """
            {"type":"/errors/no-such-code","title":"No such code","status":404,"detail":"That code does not exist",\
            "instance":"/pickups"}""";
    public static final String CODE_EXPIRED = """
            {"type":"/errors/code-expired","title":"Code expired","status":404,\
            "detail":"That code's window has passed","instance":"/pickups"}""";
    public static final String CODE_USED = """
            {"type":"/errors/code-used","title":"Code already used","status":409,\
            "detail":"That code has already opened its door","instance":"/pickups"}""";

    private ProblemExamples() {
    }
}