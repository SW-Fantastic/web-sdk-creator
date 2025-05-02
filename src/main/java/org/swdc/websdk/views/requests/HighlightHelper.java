package org.swdc.websdk.views.requests;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HighlightHelper {

    private JsonFactory jsonFactory = new JsonFactory();

    public static String jsonTokenToClassName(JsonToken jsonToken) {
        if (jsonToken == null) {
            return "";
        }
        switch (jsonToken) {
            case FIELD_NAME: return "json-property";
            case VALUE_STRING: return "json-string";
            case START_OBJECT: return "json-start-object";
            case END_OBJECT : return "json-end-object";
            case VALUE_NUMBER_FLOAT: return "json-float";
            case VALUE_NUMBER_INT: return "json-int";
            case VALUE_TRUE: return "json-true";
            case VALUE_FALSE: return "json-false";
            case START_ARRAY: return "json-start-array";
            case END_ARRAY: return "json-end-array";
            case VALUE_EMBEDDED_OBJECT: return "json-embedded";
            case VALUE_NULL: return "json-null";
            default: return "";
        }
    }

    public StyleSpans<Collection<String>> highlight(String json) throws IOException {
        List<Match> matches = new ArrayList<>();

        JsonParser parser = jsonFactory.createParser(json);
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            int start = (int) parser.currentTokenLocation().getCharOffset();
            int end = start + parser.getTextLength();

            // add surrounding ""
            if (jsonToken == JsonToken.VALUE_STRING || jsonToken == JsonToken.FIELD_NAME) {
                end += 2;
            }

            String className = jsonTokenToClassName(jsonToken);
            if (!className.isEmpty()) {
                matches.add(new Match(className, start, end));
            }
        }

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastPos = 0;
        for (Match match : matches) {
            if (match.start() > lastPos) {
                spansBuilder.add(Collections.emptyList(), match.start() - lastPos);
            }

            spansBuilder.add(Collections.singleton(match.kind()), match.end() - match.start());
            lastPos = match.end();
        }

        if (lastPos == 0) {
            spansBuilder.add(Collections.emptyList(), json.length());
        }

        return spansBuilder.create();
    }

    private static class Match implements Comparable<Match> {

        private String kind;

        private int start;

        private int end;

        public Match(String kind, int start, int end) {
            this.kind = kind;
            this.start = start;
            this.end = end;
        }

        public String kind() {
            return kind;
        }

        public int start() {
            return start;
        }

        public int end() {
            return end;
        }

        @Override
        public int compareTo(Match match) {
            return Integer.compare(start, match.start);
        }

    }

}
