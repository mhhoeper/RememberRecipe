#!/usr/bin/bash

base_addr="https://recipe.dns-cloud.net/new"

session_id=0

function f_init {
	echo "Init called"
	session_id=`curl "$base_addr/new_set" -H "Accept: application/json" 2>/dev/null | jq -r '.session_id'`
	echo "New session ID is $session_id"
}

function f_set_title {
	echo "Set title for session id $session_id"
	echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
	curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d '{"session_id":"'"$session_id"'", "title":"Spicy Chicken"}' "$base_addr/set_title"
}

function f_set_title_no_data {
	echo "Set title without data -> should error"
	curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d '{}' "$base_addr/set_title"
	# Expect error
}

function f_get_available_tags {
    echo "Get available tags"
    curl -w "%{http_code}\n" -H "Accept: application/json" "$base_addr/get_available_tags"
}

function f_set_tags {
	echo "Set tags for session id $session_id"
	echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
	curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d '{"session_id":"'"$session_id"'", "tags":"spicy fast"}' "$base_addr/set_tags"
}

function f_set_rating {
	echo "Set rating for session id $session_id"
	echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
	curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d '{"session_id":"'"$session_id"'", "rating":4}' "$base_addr/set_rating"
}

function f_set_reference {
	echo "Set reference for session id $session_id"
	echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
	curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d '{"session_id":"'"$session_id"'", "reference":"Jamie Cullum Best Dishes, page 20ff"}' "$base_addr/set_reference"
}

function f_get_available_ingredients {
    echo "Get available ingredients"
    curl -w "%{http_code}\n" -H "Accept: application/json" "$base_addr/get_available_ingredients"
}

function f_set_ingredients {
	echo "Set ingredients for session id $session_id"
	echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
	curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d '{"session_id":"'"$session_id"'", "ingredients":"chicken pepper rice chili"}' "$base_addr/set_ingredients"
}

f_add_photo() {
    echo "Add photo selection $1 for session_id $session_id"
    echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
    case $1 in
        1)   img="testimages/1997-JUGE_AUX_DOSSIERS.jpg";;
        2)   img="testimages/Abandoned_Home.jpg";;
        3)   img="testimages/Ehemalige_Burgstelle_Dickener_Schlössle.jpg";;
        4)   img="testimages/800_Houston_St_Manhattan_KS_3.jpg";;
        5)   img="testimages/Complex_Esportiu_LHospitalet_Nord_2.jpg";;
        *)   img="testimages/Freres_lamrani.jpg"  ;;
    esac
	echo '{"session_id":"'"$session_id"'", "photodata":"'"$( base64 $img )"'"}' | curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d @- "$base_addr/add_photo"    
}

function f_remove_photo_1 {
	echo "Remove photo nr 1 for session_id $session_id"
	echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
	echo '{"session_id":"'"$session_id"'", "photo_no":1}' | curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d @- "$base_addr/remove_photo"
}

function f_move_photo_2_to_1 {
	echo "Move photo nr 2 to position 1 for session_id $session_id"
	echo "Check result in https://recipe.dns-cloud.net/b?session_id=$session_id"
	echo '{"session_id":"'"$session_id"'", "photo_from":2, "photo_to":1}' | curl -X POST -w "%{http_code}\n" -H "Content-Type: application/json" -d @- "$base_addr/move_photo"
}


function error_menu {
	while [ 1 ]
	do
		PS3='Choose entry in error menu: '
		select choix in "set_title_no_data" "main_menu"
		do
			break
		done
		case $choix in
			set_title_no_data)                    f_set_title_no_data;;
			main_menu)                            return;;
			*)                                    return;;
		esac
	done

}

function main_menu {
	while [ 1 ]
	do 
		PS3='Choose entry: '
		select choix in "init" "set_title" "get_available_tags" "set_tags" "set_rating" "set_reference" "get_available_ingredients" "set_ingredients" "add_photo_1" "add_photo_2" "add_photo_3" "add_photo_4" "add_photo_5" "add_photo_6" "remove_photo_1" "move_photo_2_to_1" "quit" "errors"
		do
			break
		done
		case $choix in
			init)               f_init;;
			set_title)          f_set_title;;
			get_available_tags) f_get_available_tags;;
			set_tags)           f_set_tags;;
			set_rating)         f_set_rating;;
			set_reference)      f_set_reference;;
			get_available_ingredients)  f_get_available_ingredients;;
			set_ingredients)    f_set_ingredients;;
			add_photo_1)        f_add_photo 1;;
			add_photo_2)        f_add_photo 2;;
			add_photo_3)        f_add_photo 3;;
			add_photo_4)        f_add_photo 4;;
			add_photo_5)        f_add_photo 5;;
			add_photo_6)        f_add_photo 6;;
			remove_photo_1)     f_remove_photo_1;;
			move_photo_2_to_1)  f_move_photo_2_to_1;;
			quit)               exit ;;
			errors)             error_menu;;
			*)                  echo "unknown command" ;;
		esac
	done
}

main_menu
