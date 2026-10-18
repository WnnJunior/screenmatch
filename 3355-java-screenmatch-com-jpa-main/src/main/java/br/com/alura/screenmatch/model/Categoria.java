
package br.com.alura.screenmatch.model;

public enum Categoria {
    ACAO("Action"),
    ROMANCE("Romance"),
    COMEDIA("Comedy"),
    DRAMA("Drama"),
    CRIME("Crime"),
    AVENTURA("Adventure"),
    SUSPENSE("Thriller"),
    FICCAO_CIENTIFICA("Sci-Fi"),
    FANTASIA("Fantasy"),
    ANIMACAO("Animation"),
    DOCUMENTARIO("Documentary"),
    MISTERIO("Mystery"),
    TERROR("Horror");

    private final String categoriaOmdb;

    Categoria(String categoriaOmdb) {
        this.categoriaOmdb = categoriaOmdb;
    }

    public static Categoria fromString(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaOmdb.equalsIgnoreCase(text)) {
                return categoria;
            }
        }

        throw new IllegalArgumentException(
                "Nenhuma categoria encontrada para: " + text
        );
    }
}

