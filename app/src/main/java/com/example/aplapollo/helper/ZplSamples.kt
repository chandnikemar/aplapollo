package com.example.aplapollo.helper


object ZplBuilder {

    fun buildLabel(
        batchNo: String,
        supplierName: String,
        grade: String,
        thickness: String,
        width: String,
        grnNo: String,
        grnDate: String,
        materialCode: String,
        netWeight: String
    ): String {

        return """
^XA
^MMT
^PW609
^LL812
^FO6,5^GB596,800,8^FS
^FT32,150^A0N,28,28^FDSupplier Batch #^FS
^FT32,196^A0N,28,28^FDSupplier Name^FS
^FT32,231^A0N,28,28^FDGrade^FS
^FT32,270^A0N,28,28^FDThickness^FS
^FT32,305^A0N,28,28^FDWidth^FS
^FT32,346^A0N,28,28^FDGRN Number^FS
^FT32,392^A0N,28,28^FDGRN Date^FS
^FT32,427^A0N,28,28^FDMaterial Code^FS
^FT32,470^A0N,28,28^FDNET Weight(kg)^FS

^FT313,150^A0N,28,28^FD$batchNo^FS
^FT313,196^A0N,28,28^FD$supplierName^FS
^FT313,231^A0N,28,28^FD$grade^FS
^FT313,270^A0N,28,28^FD$thickness^FS
^FT313,305^A0N,28,28^FD$width^FS
^FT313,346^A0N,28,28^FD$grnNo^FS
^FT313,392^A0N,28,28^FD$grnDate^FS
^FT313,427^A0N,28,28^FD$materialCode^FS
^FT313,470^A0N,28,28^FD$netWeight^FS

^PQ1
^XZ
"""
    }
}
