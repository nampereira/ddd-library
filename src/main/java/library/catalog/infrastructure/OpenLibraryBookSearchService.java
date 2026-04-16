package library.catalog.infrastructure;

import library.catalog.application.BookInformation;
import library.catalog.application.BookSearchService;
import library.catalog.domain.BookInformationNotFoundException;
import library.catalog.domain.Isbn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
class OpenLibraryBookSearchService implements BookSearchService {
    private final RestClient restClient;

    public OpenLibraryBookSearchService(RestClient.Builder builder,
            @Value("${catalog.openlibrary.base-url:https://openlibrary.org/}") String baseUrl) {
        // Timeouts are configured globally via spring.http.client.connect-timeout /
        // spring.http.client.read-timeout in application.properties so that
        // @RestClientTest can replace the request factory with its mock server.
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public BookInformation search(Isbn isbn) {
        OpenLibraryIsbnSearchResult result = restClient.get().uri("isbn/{isbn}.json", isbn.value())
                .retrieve()
                .body(OpenLibraryIsbnSearchResult.class);
        if (result == null || result.title() == null || result.title().isBlank()) {
            throw new BookInformationNotFoundException(isbn);
        }
        return new BookInformation(result.title());
    }
}
