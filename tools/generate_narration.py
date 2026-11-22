"""
Membuat file audio narasi POI dari docs/audio/naskah.md memakai ElevenLabs Text-to-Speech.

API key dan voice ID dibaca dari local.properties (file ini tidak di-commit):

    ELEVENLABS_API_KEY=...
    ELEVENLABS_VOICE_ID=...        # suara untuk narasi bahasa Indonesia
    ELEVENLABS_VOICE_ID_EN=...     # opsional; kalau kosong, suara ID dipakai juga untuk EN

Contoh pemakaian (dari folder root project):

    python tools/generate_narration.py --count            # hitung karakter, tanpa memanggil API
    python tools/generate_narration.py --voices           # daftar suara di akun ElevenLabs
    python tools/generate_narration.py --only P01 --lang id   # coba satu file dulu
    python tools/generate_narration.py                    # buat semua yang belum ada
    python tools/generate_narration.py --force            # buat ulang semuanya

Hasil: app/src/main/assets/audio/p01.mp3 (ID) dan p01_en.mp3 (EN).
Hanya memakai library bawaan Python.
"""
import argparse
import json
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
NASKAH = ROOT / "docs" / "audio" / "naskah.md"
OUT_DIR = ROOT / "app" / "src" / "main" / "assets" / "audio"
PROPS = ROOT / "local.properties"

MODEL_ID = "eleven_multilingual_v2"   # mendukung bahasa Indonesia dan Inggris
OUTPUT_FORMAT = "mp3_44100_96"        # cukup jernih untuk suara, ±0,7 MB per menit
VOICE_SETTINGS = {"stability": 0.5, "similarity_boost": 0.75, "style": 0.0, "use_speaker_boost": True}
API = "https://api.elevenlabs.io/v1"


def read_props() -> dict:
    props = {}
    if PROPS.exists():
        for line in PROPS.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                k, v = line.split("=", 1)
                props[k.strip()] = v.strip()
    return props


def read_scripts() -> dict:
    """{('P01', 'id'): 'teks...', ...} dari judul '## P01 ID' / '## P01 EN'."""
    text = NASKAH.read_text(encoding="utf-8")
    parts = re.split(r"^## (P\d{2}) (ID|EN)\s*$", text, flags=re.MULTILINE)
    scripts = {}
    for i in range(1, len(parts), 3):
        body = " ".join(parts[i + 2].split())
        scripts[(parts[i], parts[i + 1].lower())] = body
    return scripts


def out_path(poi_id: str, lang: str) -> Path:
    suffix = "" if lang == "id" else "_en"
    return OUT_DIR / f"{poi_id.lower()}{suffix}.mp3"


def request(url: str, key: str, body: dict | None = None) -> bytes:
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method="POST" if data else "GET")
    req.add_header("xi-api-key", key)
    if data:
        req.add_header("Content-Type", "application/json")
        req.add_header("Accept", "audio/mpeg")
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            return resp.read()
    except urllib.error.HTTPError as e:
        detail = e.read().decode("utf-8", "replace")[:400]
        sys.exit(f"ElevenLabs menolak permintaan (HTTP {e.code}): {detail}")


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--count", action="store_true", help="hitung karakter saja")
    ap.add_argument("--voices", action="store_true", help="tampilkan daftar suara di akun")
    ap.add_argument("--only", help="ID POI, mis. P01")
    ap.add_argument("--lang", choices=["id", "en"], help="hanya satu bahasa")
    ap.add_argument("--force", action="store_true", help="timpa file yang sudah ada")
    args = ap.parse_args()

    scripts = read_scripts()
    todo = [(pid, lang) for (pid, lang) in sorted(scripts)
            if (not args.only or pid == args.only.upper()) and (not args.lang or lang == args.lang)]
    if not args.force:
        todo = [t for t in todo if not out_path(*t).exists()]

    chars = sum(len(scripts[t]) for t in todo)
    print(f"{len(todo)} file akan dibuat, total {chars} karakter.")
    if args.count:
        for t in todo:
            print(f"  {t[0]} {t[1].upper()}: {len(scripts[t])} karakter")
        return

    props = read_props()
    key = props.get("ELEVENLABS_API_KEY")
    if not key:
        sys.exit("ELEVENLABS_API_KEY belum ada di local.properties.")

    if args.voices:
        data = json.loads(request(f"{API}/voices", key))
        for v in data.get("voices", []):
            labels = v.get("labels") or {}
            print(f"{v['voice_id']}  {v['name']}  {labels.get('language', '')} {labels.get('accent', '')} {labels.get('gender', '')}")
        return

    voice_id = props.get("ELEVENLABS_VOICE_ID")
    if not voice_id:
        sys.exit("ELEVENLABS_VOICE_ID belum ada di local.properties. Jalankan --voices untuk melihat pilihan.")
    voice_en = props.get("ELEVENLABS_VOICE_ID_EN") or voice_id

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for pid, lang in todo:
        voice = voice_id if lang == "id" else voice_en
        audio = request(
            f"{API}/text-to-speech/{voice}?output_format={OUTPUT_FORMAT}",
            key,
            {"text": scripts[(pid, lang)], "model_id": MODEL_ID, "voice_settings": VOICE_SETTINGS},
        )
        path = out_path(pid, lang)
        path.write_bytes(audio)
        print(f"  {path.relative_to(ROOT)}  {len(audio) // 1024} KB")
    print("Selesai.")


if __name__ == "__main__":
    main()
