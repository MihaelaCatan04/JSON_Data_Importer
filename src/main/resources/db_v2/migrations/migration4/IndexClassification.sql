CREATE UNIQUE INDEX classification_idx
    ON classification (
                       COALESCE(code_value, ''),
                       COALESCE(code_name, ''),
                       COALESCE(japanese, '')
        )
    WHERE deleted IS false