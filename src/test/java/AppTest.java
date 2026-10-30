import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class AppTest {

    //step 2
    @Test
    void catFactKeepsTheValuesItWasGiven() {
        int id = 7;
        String text = "Cats have three eyelids";

        App.CatFact fact = new App.CatFact(id, text);

        assertEquals(id, fact.id());
        assertEquals(text, fact.fact());
    }

    //step 3
    @Test
    void twoCatFactsWithTheSameValuesAreEqual() {
        int id = 7;
        String text = "Cats have three eyelids";

        App.CatFact first = new App.CatFact(id, text);
        App.CatFact second = new App.CatFact(id, text);

        assertEquals(first, second);
    }

    //step 4
    @Test
    void twoCatFactsWithDifferentTextAreNotEqual() {
        int id = 7;

        App.CatFact first = new App.CatFact(id, "Cats have three eyelids");
        App.CatFact second = new App.CatFact(id, "A group of cats is called a clowder");

        assertNotEquals(first, second);
    }

    //step 5
    @Test
    void listFindsCatFactByItsValues() {
        List<App.CatFact> facts = new ArrayList<>();
        facts.add(new App.CatFact(1, "Cats have three eyelids"));

        boolean found = facts.contains(new App.CatFact(1, "Cats have three eyelids"));

        assertTrue(found);
    }

    //step 6
    @Test
    void newFactGivesBackTheTextItWasBuiltWith() {
        String text = "Cats have three eyelids";

        App.NewFact newFact = new App.NewFact(text);

        assertEquals(text, newFact.fact());
    }

    //step 7
    @Test
    void errorBodyGivesBackTheMessageItWasBuiltWith() {
        String message = "no fact with id 99";

        App.ErrorBody errorBody = new App.ErrorBody(message);

        assertEquals(message, errorBody.error());
    }

    //step 8
    // Proves the CatFact record does not reject empty text, so that validation has to live outside the record.
    @Test
    void catFactCanHoldEmptyText() {
        String text = "";

        App.CatFact fact = new App.CatFact(1, text);

        assertNotNull(fact);
        assertEquals(text, fact.fact());
    }

    //step 9
    @Test
    void serializingCatFactProducesJsonWithBothFields() {
        App.CatFact fact = new App.CatFact(7, "Cats have three eyelids");

        String json = App.gson.toJson(fact);

        assertTrue(json.contains("\"id\": 7"));
        assertTrue(json.contains("\"fact\": \"Cats have three eyelids\""));
    }

    //step 10
    @Test
    void jsonRequestBodyDeserializesIntoNewFact() {
        String json = "{\"fact\": \"Cats have three eyelids\"}";

        App.NewFact newFact = App.gson.fromJson(json, App.NewFact.class);

        assertEquals("Cats have three eyelids", newFact.fact());
    }

    //step 11
    @Test
    void apostropheComesOutOfGsonAsAnApostrophe() {
        App.CatFact fact = new App.CatFact(1, "A cat's whiskers measure gaps");

        String json = App.gson.toJson(fact);

        assertTrue(json.contains("cat's"));
        assertFalse(json.contains("\\u0027"));
    }

    //step 12
    @Test
    void factWithCommasSurvivesSerializingAndDeserializing() {
        App.CatFact original = new App.CatFact(9, "Cats, unlike dogs, cannot taste sweetness");

        String json = App.gson.toJson(original);
        App.CatFact result = App.gson.fromJson(json, App.CatFact.class);

        assertEquals(original, result);
    }
}