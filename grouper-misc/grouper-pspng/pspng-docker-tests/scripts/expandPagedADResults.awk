BEGIN   {IGNORECASE=1
         PAGED_ATTRIBUTE_PATTERN="^([^ ]*);range=[0-9]*-([0-9]*): .*"
         last_fetch_attribute=""
         last_fetch_dn=""
         if ( ! ldap_query_cmd ) 
         {
            print "ldap_query_cmd must be defined on commandline via --assign ldap_query_cmd=blah" 
            exit 1
         }
        }

#save the DN in case we need to query it for more values of an attribute
/^dn: / {current_dn=gensub("dn: ", s, 1)}

#we've gotten some values for an attribute, need to get more
/^([^ ]*);range=[0-9]*-([0-9]*): .*/ {
        attribute=gensub(PAGED_ATTRIBUTE_PATTERN, "\\1", 1)
        range_end=gensub(PAGED_ATTRIBUTE_PATTERN, "\\2", 1)

        #if this is a different dn or attribute than we've fetched last time
        if ( attribute != last_fetch_attribute || current_dn != last_fetch_dn ) 
        {
          last_fetch_attribute=attribute
          last_fetch_dn=current_dn
          "mktemp -t" | getline tmp
          cmd=ldap_query_cmd" -b '" current_dn "' objectclass=* '" attribute ";range=" (range_end+1) "-*'"
          s=system(cmd " > " tmp)
          if ( s != 0 ) {print "Error running " cmd; exit 1;}
          
          s=system("awk -f /scripts/expandPagedADResults.awk --assign ldap_query_cmd=" ldap_query_cmd " " tmp " | egrep -v '^$|^dn: '")
          if ( s != 0 ) {print "Error running " cmd; exit 1;}
          system("rm -f " tmp)
        }
    }

#print the lines we've read
/.*/    {print}
