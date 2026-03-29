package library.catalog;

import library.catalog.application.AddBookToCatalogUseCase;
import library.catalog.application.EditAuthorDetailsUseCase;
import library.catalog.application.ListBooksUseCase;
import library.catalog.application.RegisterBookCopyUseCase;
import library.catalog.domain.AuthorNotFoundException;
import library.catalog.domain.BookInformationNotFoundException;
import library.catalog.domain.BookNotFoundException;
import library.catalog.domain.ContactEmail;
import library.catalog.domain.DuplicateBarCodeException;
import library.catalog.domain.Isbn;
import library.common.BarCode;
import library.security.SecurityConfig;
import library.security.application.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@Import(SecurityConfig.class)
class CatalogControllerTest {

    @Autowired MockMvc mvc;

    @MockBean AddBookToCatalogUseCase addBook;
    @MockBean RegisterBookCopyUseCase registerCopy;
    @MockBean ListBooksUseCase listBooks;
    @MockBean EditAuthorDetailsUseCase editAuthor;
    @MockBean JwtService jwtService;

    // ─── GET /catalog/books ───────────────────────────────────────────────────

    @Test
    void getBooksReturns200WithoutAuthentication() throws Exception {
        when(listBooks.listAll()).thenReturn(List.of());

        mvc.perform(get("/catalog/books"))
           .andExpect(status().isOk());
    }

    // ─── POST /catalog/books ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void addBookReturns201WhenSuccessful() throws Exception {
        mvc.perform(post("/catalog/books").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                       {"isbn":"9780132350884",
                        "authors":[{"name":"Robert C. Martin","contactEmail":"uncle.bob@example.com"}]}
                       """))
           .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void addBookReturns404WhenBookInfoNotFound() throws Exception {
        doThrow(new BookInformationNotFoundException(new Isbn("9780132350884")))
                .when(addBook).execute(any(), any());

        mvc.perform(post("/catalog/books").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                       {"isbn":"9780132350884",
                        "authors":[{"name":"Author","contactEmail":"a@b.com"}]}
                       """))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("No book information found for ISBN '9780132350884'"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void addBookReturns400WhenRequestBodyIsInvalid() throws Exception {
        mvc.perform(post("/catalog/books").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"isbn\":\"\",\"authors\":[]}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void addBookReturns403WhenNotAuthenticated() throws Exception {
        mvc.perform(post("/catalog/books").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                       {"isbn":"9780132350884",
                        "authors":[{"name":"Author","contactEmail":"a@b.com"}]}
                       """))
           .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void addBookReturns403ForPatronRole() throws Exception {
        mvc.perform(post("/catalog/books").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                       {"isbn":"9780132350884",
                        "authors":[{"name":"Author","contactEmail":"a@b.com"}]}
                       """))
           .andExpect(status().isForbidden());
    }

    // ─── POST /catalog/copies ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void registerCopyReturns201WhenSuccessful() throws Exception {
        mvc.perform(post("/catalog/copies").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"isbn\":\"9780132350884\",\"barCode\":\"BC-001\"}"))
           .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void registerCopyReturns404WhenBookNotFound() throws Exception {
        doThrow(new BookNotFoundException(new Isbn("9780132350884")))
                .when(registerCopy).execute(any(), any());

        mvc.perform(post("/catalog/copies").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"isbn\":\"9780132350884\",\"barCode\":\"BC-001\"}"))
           .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void registerCopyReturns409WhenBarcodeAlreadyExists() throws Exception {
        doThrow(new DuplicateBarCodeException(new BarCode("BC-001")))
                .when(registerCopy).execute(any(), any());

        mvc.perform(post("/catalog/copies").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"isbn\":\"9780132350884\",\"barCode\":\"BC-001\"}"))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.message").value("A copy with barcode 'BC-001' already exists"));
    }

    // ─── PATCH /catalog/authors/{email} ──────────────────────────────────────

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void editAuthorReturns204WhenSuccessful() throws Exception {
        mvc.perform(patch("/catalog/authors/uncle.bob@example.com").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"bio\":\"The author of Clean Code\"}"))
           .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void editAuthorReturns404WhenAuthorNotFound() throws Exception {
        ContactEmail email = new ContactEmail("unknown@example.com");
        doThrow(new AuthorNotFoundException(email))
                .when(editAuthor).execute(any(), any());

        mvc.perform(patch("/catalog/authors/unknown@example.com").with(csrf())
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"bio\":\"Some bio\"}"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Author with email 'unknown@example.com' not found"));
    }
}
