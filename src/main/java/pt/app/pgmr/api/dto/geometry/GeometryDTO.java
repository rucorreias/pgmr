package pt.app.pgmr.api.dto.geometry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GeometryDTO(

        @NotBlank
        String type,

        @NotNull
        List<List<Double>> coordinates
) {
}