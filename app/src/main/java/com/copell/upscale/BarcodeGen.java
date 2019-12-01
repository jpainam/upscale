package com.copell.upscale;

import android.content.Intent;

import com.copell.upscale.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import androidx.annotation.NonNull;


public class BarcodeGen {

      FirebaseFirestore db = FirebaseFirestore.getInstance();

        private void generateQRCodeImage(String id, String name, int price, String filePath)
                throws WriterException, IOException {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(String.format("%s#%s#%s", id,
                    name, price), BarcodeFormat.QR_CODE, price, price);


            Path path = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                path = FileSystems.getDefault().getPath(filePath);
            }
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        }

        public void generateAll() throws Exception{
            db.collection("products").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot snapshots = task.getResult();
                    for(DocumentSnapshot doc : snapshots){
                        Product p = doc.toObject(Product.class);
                        try {
                            generateQRCodeImage(doc.getId(), p.getName(), p.getPrice(),
                                    String.format("%s.png", doc.getId()));
                        } catch (WriterException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        /*public static void main(String[] args) {
            try {

                //generateQRCodeImage("wkjef4849892D", "Shirt", 350, QR_CODE_IMAGE_PATH);
                generateAll();
            } catch (WriterException e) {
                System.out.println("Could not generate QR Code, WriterException :: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Could not generate QR Code, IOException :: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
}