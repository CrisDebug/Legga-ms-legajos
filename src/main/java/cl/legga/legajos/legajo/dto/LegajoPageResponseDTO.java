package cl.legga.legajos.legajo.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public class LegajoPageResponseDTO {

    private final List<LegajoResponseDTO> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public LegajoPageResponseDTO(Page<LegajoResponseDTO> resultado) {
        this.content = resultado.getContent();
        this.page = resultado.getNumber();
        this.size = resultado.getSize();
        this.totalElements = resultado.getTotalElements();
        this.totalPages = resultado.getTotalPages();
        this.first = resultado.isFirst();
        this.last = resultado.isLast();
    }

    public List<LegajoResponseDTO> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}