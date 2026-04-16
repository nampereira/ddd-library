package library.catalog.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record OpenLibraryIsbnSearchResult(
        List<String> publishers,
        String title,
        @JsonProperty("isbn_13") List<String> isbn13,
        int revisions) {
}
