package com.example.socialapp;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class LinearSearch {
    public static ArrayList<GeoPoint> getKNearestList(GeoPoint target, int k, Context context) {
        try {
            String line;
            int sortsCount = 1;
            ArrayList<GeoPoint> kNearestList = new ArrayList<>();
            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(MainActivity.unsorted_input)));

            for (int i=0;i<k;i++){
                line = file.readLine();
                if (line==null){
                    System.out.println("File has k less points!");
                    break;
                }
                int br = line.indexOf("-");
                char[] chars = line.toCharArray();
                char[] lon = Arrays.copyOfRange(chars, 0, br);
                char[] lat = Arrays.copyOfRange(chars, br+1, chars.length);
                kNearestList.add(new GeoPoint(String.valueOf(lon), String.valueOf(lat)));
            }

            Collections.sort(Objects.requireNonNull(kNearestList), (o1, o2) -> {
                Float f1 = o1.distanceTo(target);
                Float f2 = o2.distanceTo(target);
                return f1.compareTo(f2);
            });

            line = file.readLine();

            while (line != null) {
                int br = line.indexOf("-");
                char[] chars = line.toCharArray();
                char[] lon = Arrays.copyOfRange(chars, 0, br);
                char[] lat = Arrays.copyOfRange(chars, br+1, chars.length);
                GeoPoint temp = new GeoPoint(String.valueOf(lon),String.valueOf(lat));

                if (temp.distanceTo(target)<kNearestList.get(kNearestList.size()-1).distanceTo(target)){
                    kNearestList.remove(kNearestList.size()-1);
                    kNearestList.add(temp);

                    Collections.sort(Objects.requireNonNull(kNearestList), (o1, o2) -> {
                        Float f1 = o1.distanceTo(target);
                        Float f2 = o2.distanceTo(target);
                        return f1.compareTo(f2);
                    });
                    sortsCount++;

                }
                line = file.readLine();
            }
            file.close();
            System.out.println("Sorted "+ sortsCount + " times!");
            return kNearestList;
        } catch (Exception e) {
            System.out.println("Error in Linear Search!");
            e.printStackTrace();
            return null;
        }
    }
}
