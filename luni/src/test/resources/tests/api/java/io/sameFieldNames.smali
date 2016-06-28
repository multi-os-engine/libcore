# Source for sameFieldNames.dex
.class public LsameFieldNames;
.super Ljava/lang/Object;
.implements Ljava/io/Serializable;

# Test multiple fields with the same name and different types.
# (Invalid in Java language but valid in bytecode.)
.field public a:D
.field public a:S
.field public a:J
.field public a:F
.field public a:Z
.field public a:I
.field public a:B
.field public a:C
.field public a:Ljava/lang/Integer;
.field public a:Ljava/lang/Long;
.field public a:Ljava/lang/Float;
.field public a:Ljava/lang/Double;
.field public a:Ljava/lang/Boolean;
.field public a:Ljava/lang/Void;
.field public a:Ljava/lang/Short;
.field public a:Ljava/lang/Character;
.field public a:Ljava/lang/Byte;
