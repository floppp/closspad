We are going to use supabase but querying it through api so no js client needed.

+ Query matches
```bash
curl 'https://orqwiubnozfouaqyywah.supabase.co/rest/v1/CLOSSPAD_match?select=*&order=played_at.asc' \
    -H "apikey: SUPABASE_CLIENT_ANON_KEY" \
    -H "Authorization: Bearer SUPABASE_CLIENT_ANON_KEY"
```


## Pages

### Matches/classification
/match/2025-04-07
