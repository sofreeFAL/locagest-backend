package sn.uidt.locagest.locagest_backend.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.ContratLocation;
import sn.uidt.locagest.locagest_backend.model.Location;

import java.awt.*;
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
            // Couleurs personnalisées
            Color orangeCustom = new Color(255, 140, 0);   // FF8C00 en RGB


            // ===== EN-TÊTE AVEC LOGO ET NOM =====
            Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, orangeCustom);
            Paragraph title = new Paragraph("LocaGest", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph subtitle = new Paragraph("Contrat de Location de Véhicule", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // ===== SECTION INFOS CONTRAT =====
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLUE);
            Paragraph sectionContrat = new Paragraph("INFORMATIONS DU CONTRAT", sectionFont);
            sectionContrat.setSpacingBefore(10);
            document.add(sectionContrat);

            // Ligne séparatrice
            document.add(new Chunk("\n"));

            // Tableau pour informations contrat - Correction : utiliser PdfPTable
            PdfPTable tableContrat = new PdfPTable(2);
            tableContrat.setWidthPercentage(100);
            tableContrat.setSpacingBefore(5);
            tableContrat.setSpacingAfter(5);

            // Style des cellules
            Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 11);

            PdfPCell cellLabel1 = new PdfPCell(new Phrase("Numéro du contrat :", labelFont));
            cellLabel1.setBorder(0);
            PdfPCell cellValue1 = new PdfPCell(new Phrase(contrat.getNumeroContrat(), valueFont));
            cellValue1.setBorder(0);

            PdfPCell cellLabel2 = new PdfPCell(new Phrase("Date de création :", labelFont));
            cellLabel2.setBorder(0);
            PdfPCell cellValue2 = new PdfPCell(new Phrase(contrat.getDateCreation().format(df), valueFont));
            cellValue2.setBorder(0);

            PdfPCell cellLabel3 = new PdfPCell(new Phrase("Statut :", labelFont));
            cellLabel3.setBorder(0);
            Font statusFont = new Font(Font.HELVETICA, 11, Font.BOLD,
                    "TERMINE".equals(contrat.getStatut()) ? Color.GREEN : Color.ORANGE);
            PdfPCell cellValue3 = new PdfPCell(new Phrase(contrat.getStatut().toString(), statusFont));
            cellValue3.setBorder(0);

            tableContrat.addCell(cellLabel1);
            tableContrat.addCell(cellValue1);
            tableContrat.addCell(cellLabel2);
            tableContrat.addCell(cellValue2);
            tableContrat.addCell(cellLabel3);
            tableContrat.addCell(cellValue3);

            document.add(tableContrat);

            // ===== SECTION CLIENT =====
            Paragraph sectionClient = new Paragraph("CLIENT", sectionFont);
            sectionClient.setSpacingBefore(20);
            document.add(sectionClient);

            document.add(new Chunk("\n"));

            PdfPTable tableClient = new PdfPTable(2);
            tableClient.setWidthPercentage(100);
            tableClient.setSpacingBefore(5);
            tableClient.setSpacingAfter(5);

            PdfPCell clientLabel1 = new PdfPCell(new Phrase("Nom complet :", labelFont));
            clientLabel1.setBorder(0);
            PdfPCell clientValue1 = new PdfPCell(new Phrase(
                    location.getClient().getNom() + " " + location.getClient().getPrenom(), valueFont));
            clientValue1.setBorder(0);

            PdfPCell clientLabel2 = new PdfPCell(new Phrase("Email :", labelFont));
            clientLabel2.setBorder(0);
            PdfPCell clientValue2 = new PdfPCell(new Phrase(location.getClient().getEmail(), valueFont));
            clientValue2.setBorder(0);

            PdfPCell clientLabel3 = new PdfPCell(new Phrase("Téléphone :", labelFont));
            clientLabel3.setBorder(0);
            PdfPCell clientValue3 = new PdfPCell(new Phrase(location.getClient().getTelephone(), valueFont));
            clientValue3.setBorder(0);

            PdfPCell clientLabel4 = new PdfPCell(new Phrase("CNI :", labelFont));
            clientLabel4.setBorder(0);
            PdfPCell clientValue4 = new PdfPCell(new Phrase(location.getClient().getNumeroCni(), valueFont));
            clientValue4.setBorder(0);

            tableClient.addCell(clientLabel1);
            tableClient.addCell(clientValue1);
            tableClient.addCell(clientLabel2);
            tableClient.addCell(clientValue2);
            tableClient.addCell(clientLabel3);
            tableClient.addCell(clientValue3);
            tableClient.addCell(clientLabel4);
            tableClient.addCell(clientValue4);

            document.add(tableClient);

            // ===== SECTION VÉHICULE =====
            Paragraph sectionVehicule = new Paragraph("VÉHICULE", sectionFont);
            sectionVehicule.setSpacingBefore(20);
            document.add(sectionVehicule);

            document.add(new Chunk("\n"));

            PdfPTable tableVehicule = new PdfPTable(2);
            tableVehicule.setWidthPercentage(100);
            tableVehicule.setSpacingBefore(5);
            tableVehicule.setSpacingAfter(5);

            PdfPCell vehiculeLabel1 = new PdfPCell(new Phrase("Marque :", labelFont));
            vehiculeLabel1.setBorder(0);
            PdfPCell vehiculeValue1 = new PdfPCell(new Phrase(location.getVehicule().getMarque(), valueFont));
            vehiculeValue1.setBorder(0);

            PdfPCell vehiculeLabel2 = new PdfPCell(new Phrase("Modèle :", labelFont));
            vehiculeLabel2.setBorder(0);
            PdfPCell vehiculeValue2 = new PdfPCell(new Phrase(location.getVehicule().getModele(), valueFont));
            vehiculeValue2.setBorder(0);

            PdfPCell vehiculeLabel3 = new PdfPCell(new Phrase("Immatriculation :", labelFont));
            vehiculeLabel3.setBorder(0);
            PdfPCell vehiculeValue3 = new PdfPCell(new Phrase(location.getVehicule().getImmatriculation(), valueFont));
            vehiculeValue3.setBorder(0);

            PdfPCell vehiculeLabel4 = new PdfPCell(new Phrase("Prix journalier :", labelFont));
            vehiculeLabel4.setBorder(0);
            Font priceFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.GREEN);
            PdfPCell vehiculeValue4 = new PdfPCell(new Phrase(
                    String.format("%,.0f", location.getVehicule().getPrixParJour()) + " FCFA", priceFont));
            vehiculeValue4.setBorder(0);

            tableVehicule.addCell(vehiculeLabel1);
            tableVehicule.addCell(vehiculeValue1);
            tableVehicule.addCell(vehiculeLabel2);
            tableVehicule.addCell(vehiculeValue2);
            tableVehicule.addCell(vehiculeLabel3);
            tableVehicule.addCell(vehiculeValue3);
            tableVehicule.addCell(vehiculeLabel4);
            tableVehicule.addCell(vehiculeValue4);

            document.add(tableVehicule);

            // ===== SECTION LOCATION =====
            Paragraph sectionLocation = new Paragraph("LOCATION", sectionFont);
            sectionLocation.setSpacingBefore(20);
            document.add(sectionLocation);

            document.add(new Chunk("\n"));

            PdfPTable tableLocation = new PdfPTable(2);
            tableLocation.setWidthPercentage(100);
            tableLocation.setSpacingBefore(5);
            tableLocation.setSpacingAfter(5);

            PdfPCell locationLabel1 = new PdfPCell(new Phrase("Date début :", labelFont));
            locationLabel1.setBorder(0);
            PdfPCell locationValue1 = new PdfPCell(new Phrase(location.getDateDebut().format(df), valueFont));
            locationValue1.setBorder(0);

            PdfPCell locationLabel2 = new PdfPCell(new Phrase("Date fin :", labelFont));
            locationLabel2.setBorder(0);
            PdfPCell locationValue2 = new PdfPCell(new Phrase(location.getDateFin().format(df), valueFont));
            locationValue2.setBorder(0);

            PdfPCell locationLabel3 = new PdfPCell(new Phrase("Durée :", labelFont));
            locationLabel3.setBorder(0);
            long days = java.time.temporal.ChronoUnit.DAYS.between(location.getDateDebut(), location.getDateFin());
            PdfPCell locationValue3 = new PdfPCell(new Phrase(days + " jours", valueFont));
            locationValue3.setBorder(0);

            PdfPCell locationLabel4 = new PdfPCell(new Phrase("Montant total :", labelFont));
            locationLabel4.setBorder(0);
            Font totalFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.RED);
            PdfPCell locationValue4 = new PdfPCell(new Phrase(
                    String.format("%,.0f", location.getMontantTotalLocation()) + " FCFA", totalFont));
            locationValue4.setBorder(0);

            tableLocation.addCell(locationLabel1);
            tableLocation.addCell(locationValue1);
            tableLocation.addCell(locationLabel2);
            tableLocation.addCell(locationValue2);
            tableLocation.addCell(locationLabel3);
            tableLocation.addCell(locationValue3);
            tableLocation.addCell(locationLabel4);
            tableLocation.addCell(locationValue4);

            document.add(tableLocation);

            // ===== PIED DE PAGE =====
            document.add(new Chunk("\n\n"));

            Font footerFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            Paragraph footer = new Paragraph(
                    "Le présent contrat est généré automatiquement par le système LocaGest.\n" +
                            "Pour toute réclamation, contactez-nous au +221 77 822 84 60 ou à contact@locagest.sn",
                    footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            // ===== SIGNATURE =====
            document.add(new Chunk("\n\n"));
            Paragraph signature = new Paragraph("Signature du client", new Font(Font.HELVETICA, 10));
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            // Ligne de signature
            document.add(new Chunk("\n"));
            Paragraph signatureLine = new Paragraph("___________________________________________");
            signatureLine.setAlignment(Element.ALIGN_RIGHT);
            document.add(signatureLine);
            document.add(new Chunk("\n\n"));

            Paragraph dateSignature = new Paragraph("Date : ____________________", new Font(Font.HELVETICA, 10));
            dateSignature.setAlignment(Element.ALIGN_RIGHT);
            document.add(dateSignature);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF contrat", e);
        }
    }
}