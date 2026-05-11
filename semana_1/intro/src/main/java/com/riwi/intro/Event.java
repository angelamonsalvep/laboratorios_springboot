package com.riwi.intro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Modelo que representa un evento en el sistema")
public class Event {
    @Schema(description = "ID único del evento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre descriptivo del evento", example = "Concierto de Rock")
    private String name;

    @Schema(description = "Precio de la entrada", example = "45.50")
    private Double price;
}
