package org.superbiz.moviefun.albums;

import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    public static final String DEFAULT_COVER_JPG = "default-cover.jpg";
    private final AlbumsBean albumsBean;
    private BlobStore store;


    public AlbumsController(AlbumsBean albumsBean, BlobStore store) {

        this.albumsBean = albumsBean;
        this.store = store;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {

        Blob blob = new Blob(String.valueOf(albumId), uploadedFile.getInputStream(), uploadedFile.getContentType());
        store.put(blob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        Optional<Blob> maybeBlob = store.get(String.valueOf(albumId));
        byte[] bytes;
        String contentType;

        Blob imageBlob;

        if (maybeBlob.isPresent()) {
            imageBlob = maybeBlob.get();
        } else {
            imageBlob = defaultImageBlob();
        }
        bytes = IOUtils.toByteArray(imageBlob.inputStream);
        contentType = imageBlob.contentType;

        HttpHeaders headers = createImageHttpHeaders(bytes, contentType);

        return new HttpEntity<>(bytes, headers);
    }

    private Blob defaultImageBlob() throws IOException {
//        return store.get(DEFAULT_COVER_JPG).get();
        return new Blob("default-cover.jpg", this.getClass().getClassLoader().getResourceAsStream("default-cover.jpg"), "image/jpeg");
    }


    private HttpHeaders createImageHttpHeaders(byte[] imageBytes, String contentType) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }


}
