#! /usr/bin/perl

#
# === Prints the hvs.map - type list
#

 $nx=22; $ny=32; 
 $icr=1; $imod=0; $ich=0;

 $i=0;
 for($ix=0; $ix < $nx; $ix++) {
       for($iy=0; $iy < $ny; $iy++) {
         $id=$icr*10000+$imod*100+$ich;
#         printf(" %d %d %d %d %d \n", $i,$id,$icr,$imod,$ich);
         $idl[$i]=$id;
         $i++; $ich++;
         if ($ich > 11) {
            $ich=0; $imod++;
            if ($imod > 15) {
               $imod=0; $icr++; 
            }
         }
       }
 }

 for($iy=0; $iy < $ny; $iy++) {
   for($ix=0; $ix < $nx; $ix++) {
       $i=$iy+$ix*$ny;
         printf("%5d ",$idl[$i]);
   }
   printf("\n");
 }   

