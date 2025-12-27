package sn.uidt.locagest.locagest_backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.model.Location;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ContratPdfService {

    public byte[] genererContrat(ContratLocation contrat) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            Location location = contrat.getLocation();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // ===== TITRE =====
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("CONTRAT DE LOCATION DE VÉHICULE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // ===== INFOS CONTRAT =====
            document.add(new Paragraph("Numéro du contrat : " + contrat.getNumeroContrat()));
            document.add(new Paragraph("Date de création : " + contrat.getDateCreation().format(df)));
            document.add(new Paragraph("Statut : " + contrat.getStatut()));

            document.add(Chunk.NEWLINE);

            // ===== CLIENT =====
            document.add(new Paragraph("CLIENT"));
            document.add(new Paragraph("Nom : " + location.getClient().getNom()));
            document.add(new Paragraph("Email : " + location.getClient().getEmail()));

            document.add(Chunk.NEWLINE);

            // ===== VÉHICULE =====
            document.add(new Paragraph("VÉHICULE"));
            document.add(new Paragraph("Marque : " + location.getVehicule().getMarque()));
            document.add(new Paragraph("Modèle : " + location.getVehicule().getModele()));
            document.add(new Paragraph("Immatriculation : " + location.getVehicule().getImmatriculation()));
            document.add(new Paragraph("Prix / jour : " + location.getVehicule().getPrixParJour()));

            document.add(Chunk.NEWLINE);

            // ===== LOCATION =====
            document.add(new Paragraph("LOCATION"));
            document.add(new Paragraph("Date début : " + location.getDateDebut().format(df)));
            document.add(new Paragraph("Date fin : " + location.getDateFin().format(df)));
            document.add(new Paragraph("Montant total : " + location.getMontantTotalLocation() + " FCFA"));

            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(
                    "Le présent contrat est généré automatiquement par le système LocaGest."
            ));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF contrat", e);
        }
    }
}
