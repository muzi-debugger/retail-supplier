
CREATE TABLE products (
    id UUID PRIMARY KEY,
    product_code VARCHAR(64) NOT NULL UNIQUE,
    brand VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_products_code_not_blank
        CHECK (LENGTH(TRIM(product_code)) > 0),

    CONSTRAINT chk_products_brand_not_blank
        CHECK (LENGTH(TRIM(brand)) > 0),

    CONSTRAINT chk_products_name_not_blank
        CHECK (LENGTH(TRIM(name)) > 0)
);

CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL UNIQUE,
    colour VARCHAR(100) NOT NULL,
    size VARCHAR(20) NOT NULL,
    wholesale_price NUMERIC(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'ZAR',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT uq_product_variant_colour_size
        UNIQUE (product_id, colour, size),

    CONSTRAINT chk_variant_sku_not_blank
        CHECK (LENGTH(TRIM(sku)) > 0),

    CONSTRAINT chk_variant_colour_not_blank
        CHECK (LENGTH(TRIM(colour)) > 0),

    CONSTRAINT chk_variant_size_not_blank
        CHECK (LENGTH(TRIM(size)) > 0),

    CONSTRAINT chk_variant_price_non_negative
        CHECK (wholesale_price >= 0),

    CONSTRAINT chk_variant_currency_format
        CHECK (currency ~ '^[A-Z]{3}$')
);

CREATE INDEX idx_product_variants_product_id
    ON product_variants(product_id);

CREATE INDEX idx_products_brand
    ON products(brand);
