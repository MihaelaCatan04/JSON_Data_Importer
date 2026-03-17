CREATE UNIQUE INDEX item_idx
    ON item_info (
                  value,
                  is_industry
        )
    WHERE deleted IS false