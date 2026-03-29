package library.catalog.infrastructure;

import library.catalog.application.BookInformation;
import library.catalog.domain.BookInformationNotFoundException;
import library.catalog.domain.Isbn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(components = OpenLibraryBookSearchService.class)
@TestPropertySource(properties = "catalog.openlibrary.base-url=https://openlibrary.org/")
class OpenLibraryBookSearchServiceTest {

    @Autowired MockRestServiceServer server;
    @Autowired OpenLibraryBookSearchService service;

    private static final Isbn ISBN = new Isbn("9780132350884");

    @Test
    void returnsBookInfoForValidIsbn() {
        server.expect(requestTo("https://openlibrary.org/isbn/9780132350884.json"))
              .andRespond(withSuccess("""
                      {"title":"Clean Code","publishers":[],"isbn_13":[],"revisions":1}
                      """, MediaType.APPLICATION_JSON));

        BookInformation info = service.search(ISBN);

        assertThat(info.title()).isEqualTo("Clean Code");
    }

    @Test
    void throwsBookInfoNotFoundWhenTitleIsAbsentFromResponse() {
        server.expect(requestTo("https://openlibrary.org/isbn/9780132350884.json"))
              .andRespond(withSuccess("""
                      {"publishers":[],"isbn_13":[],"revisions":1}
                      """, MediaType.APPLICATION_JSON));

        assertThatExceptionOfType(BookInformationNotFoundException.class)
                .isThrownBy(() -> service.search(ISBN))
                .withMessageContaining(ISBN.value());
    }

    @Test
    void throwsBookInfoNotFoundWhenTitleIsBlank() {
        server.expect(requestTo("https://openlibrary.org/isbn/9780132350884.json"))
              .andRespond(withSuccess("""
                      {"title":"   ","publishers":[],"isbn_13":[],"revisions":1}
                      """, MediaType.APPLICATION_JSON));

        assertThatExceptionOfType(BookInformationNotFoundException.class)
                .isThrownBy(() -> service.search(ISBN));
    }
}
