BEGIN {
    in_block = 0
}
{
    if ($0 ~ /HardwareAccelerator\.CPU_MULTITHREAD -> "CPU \(4-Core\)"/) {
        print $0
        print "                                        },"
        print "                                        "
        print "                                        color = if (isAccSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else TextSecondary,"
        print "                                        fontSize = 11.sp,"
        print "                                        fontWeight = if (isAccSelected) FontWeight.Bold else FontWeight.Normal"
        print "                                    )"
        print "                                }"
        print "                            }"
        print "                        }"
        in_block = 1
    } else if (in_block) {
        if ($0 ~ /Spacer\(modifier = Modifier.height\(14.dp\)\)/) {
            in_block = 0
            print "                    }"
            print $0
        }
    } else {
        print $0
    }
}
