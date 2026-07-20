CREATE TABLE route_seo_redirects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    old_slug VARCHAR(180) NOT NULL,
    new_slug VARCHAR(180) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_route_seo_redirect_old_slug UNIQUE (old_slug)
);
