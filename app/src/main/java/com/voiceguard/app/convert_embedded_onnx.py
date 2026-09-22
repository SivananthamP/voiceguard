import os
import sys
import torch
import onnx

sys.path.insert(
    0,
    r"C:\Users\Krithisha\Downloads"
)

from aasist_model import AASISTFull


# ============================================================
# PATHS
# ============================================================

CHECKPOINT = r"C:\Users\Krithisha\Downloads\traindataset\aasist_training_output\best_aasistmodel.pth"

OUTPUT = r"C:\Users\Krithisha\Downloads\traindataset\aasist_training_output\aasistmodel_android.onnx"


# ============================================================
# DEVICE
# ============================================================

device = torch.device("cpu")


# ============================================================
# CHECK FILES
# ============================================================

print("========================================")
print("AASIST -> ANDROID ONNX EXPORT")
print("========================================")

if not os.path.isfile(CHECKPOINT):
    raise FileNotFoundError(
        f"Checkpoint not found:\n{CHECKPOINT}"
    )

print("Checkpoint found:")
print(CHECKPOINT)


# ============================================================
# CREATE EXACT TRAINED MODEL
# ============================================================

print()
print("Creating AASISTFull model...")

model = AASISTFull(
    node_dim=256,
    embed_dim=256,
    gat_heads=4,
    gat_layers=3
)

model.to(device)
model.eval()

print("Model created.")


# ============================================================
# LOAD CHECKPOINT
# ============================================================

print()
print("Loading checkpoint...")

checkpoint = torch.load(
    CHECKPOINT,
    map_location=device
)

model.load_state_dict(
    checkpoint["model_state_dict"]
)

model.eval()

print("Checkpoint loaded successfully.")


# ============================================================
# DUMMY INPUT
#
# 16 kHz × 3 seconds = 48,000 samples
#
# [batch, channel, samples]
# [1, 1, 48000]
# ============================================================

dummy_input = torch.randn(
    1,
    1,
    48000,
    dtype=torch.float32,
    device=device
)

print()
print("Dummy input shape:")
print(tuple(dummy_input.shape))


# ============================================================
# TEST PYTORCH MODEL FIRST
# ============================================================

print()
print("Testing PyTorch model...")

with torch.no_grad():
    pytorch_output = model(dummy_input)

print("PyTorch output:")
print(pytorch_output)

print("PyTorch output shape:")
print(
    tuple(pytorch_output.shape)
    if hasattr(pytorch_output, "shape")
    else type(pytorch_output)
)


# ============================================================
# EXPORT
# ============================================================

print()
print("Exporting ONNX model...")
print("Please wait...")

torch.onnx.export(
    model,
    dummy_input,
    OUTPUT,

    input_names=["audio"],
    output_names=["logit"],

    opset_version=17,

    dynamic_axes={
        "audio": {0: "batch"},
        "logit": {0: "batch"}
    },

    do_constant_folding=True,

    # IMPORTANT:
    # Keep weights inside the ONNX file.
    external_data=False
)


# ============================================================
# CHECK OUTPUT FILE
# ============================================================

print()
print("ONNX export finished.")

if not os.path.isfile(OUTPUT):
    raise RuntimeError(
        "ONNX file was not created."
    )

size_bytes = os.path.getsize(OUTPUT)
size_mb = size_bytes / (1024 * 1024)

print()
print("Output:")
print(OUTPUT)

print()
print("ONNX file size:")
print(f"{size_bytes:,} bytes")
print(f"{size_mb:.2f} MB")


# ============================================================
# CHECK FOR .DATA FILE
# ============================================================

data_file = OUTPUT + ".data"

print()
print("Checking external data...")

if os.path.exists(data_file):

    print("WARNING:")
    print("External data file exists:")
    print(data_file)

else:

    print("GOOD:")
    print("No .onnx.data file exists.")


# ============================================================
# VALIDATE ONNX
# ============================================================

print()
print("Checking ONNX model...")

onnx_model = onnx.load(
    OUTPUT,
    load_external_data=False
)

onnx.checker.check_model(
    onnx_model
)

print("ONNX model structure is valid.")


# ============================================================
# CHECK EXTERNAL DATA REFERENCES
# ============================================================

external_tensors = []

for initializer in onnx_model.graph.initializer:

    if initializer.external_data:
        external_tensors.append(
            initializer.name
        )

print()
print("External-data tensors:")

if external_tensors:

    print("WARNING: model still contains external data.")
    print("Number:", len(external_tensors))

    for name in external_tensors[:10]:
        print(" -", name)

else:

    print("NONE")
    print("All weights are embedded inside the ONNX file.")


# ============================================================
# FINAL
# ============================================================

print()
print("========================================")
print("EXPORT COMPLETE")
print("========================================")

print()
print("Android model:")
print(OUTPUT)

print()
print("Expected:")
print("  Single .onnx file")
print("  No .onnx.data dependency")
print("  Input: [1, 1, 48000]")
print("  Output: logit")

print()
print("========================================")
