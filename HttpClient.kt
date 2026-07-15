name: Publicar Checkpoint APK

on:
  workflow_dispatch:
    inputs:
      version:
        description: "Tag de la versión, por ejemplo v1.0.0"
        required: true
        default: "v1.0.0"
        type: string

permissions:
  contents: write

jobs:
  release:
    name: Compilar y publicar APK
    runs-on: ubuntu-latest

    steps:
      - name: Descargar proyecto
        uses: actions/checkout@v4

      - name: Configurar Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Configurar Android SDK
        uses: android-actions/setup-android@v3

      - name: Instalar SDK Android 35
        run: sdkmanager "platforms;android-35" "build-tools;35.0.0"

      - name: Configurar Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Dar permiso al wrapper
        run: chmod +x gradlew

      - name: Validar tag
        run: |
          if [[ ! "${{ inputs.version }}" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            echo "La versión debe usar el formato v1.0.0"
            exit 1
          fi

      - name: Ejecutar pruebas
        run: ./gradlew testDebugUnitTest --stacktrace

      - name: Compilar APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Preparar archivos
        run: |
          mkdir -p release
          cp app/build/outputs/apk/debug/app-debug.apk release/Checkpoint.apk
          sha256sum release/Checkpoint.apk > release/Checkpoint.apk.sha256

      - name: Crear GitHub Release
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          gh release create "${{ inputs.version }}" \
            release/Checkpoint.apk \
            release/Checkpoint.apk.sha256 \
            --target main \
            --title "Checkpoint Android ${{ inputs.version }}" \
            --generate-notes
