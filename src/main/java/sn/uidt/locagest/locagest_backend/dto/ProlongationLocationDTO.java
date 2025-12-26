package sn.uidt.locagest.locagest_backend.dto;

import java.time.LocalDate;

public class ProlongationLocationDTO {

    private LocalDate nouvelleDateFin;

    public LocalDate getNouvelleDateFin() {
        return nouvelleDateFin;
    }

    public void setNouvelleDateFin(LocalDate nouvelleDateFin) {
        this.nouvelleDateFin = nouvelleDateFin;
    }
}
